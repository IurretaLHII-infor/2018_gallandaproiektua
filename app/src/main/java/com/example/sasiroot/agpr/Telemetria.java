package com.example.sasiroot.agpr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import DB.DataObject;
import DB.GallandaDbSchema;

import static com.example.sasiroot.agpr.MainActivity.bluetoothIn;
import static com.example.sasiroot.agpr.MainActivity.mConnectedThread;


public class Telemetria extends Fragment implements OnChartValueSelectedListener {
    private ImageView locker;
    private ImageView seeData;
    private boolean getData = false;

    private Date date;

    protected static List<Float> SpeedList = new ArrayList<>();
    protected static List<Float> TemperatureList = new ArrayList<>();
    protected static List<Float> BatteryList = new ArrayList<>();

    Handler DataHandler;
    ThreadData threadData;

    List<String> VelDataList;
    List<Entry> entries;

    //private VideoView video;
    private OnFragmentInteractionListener mListener;

    //Chart
    private LineChart lineChartVel;
    private LineChart lineChartBat;
    private LineChart lineChartTmp;
    private TextView txtVel;
    private TextView txtBat;
    private TextView txtTmp;

    private String message = ":0000000000F000000\n";

    final int handlerState = 0;                         //used to identify handler message
    private StringBuilder recDataString = new StringBuilder();
    String speed;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
    private static final SimpleDateFormat formatTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    int[] mColors = ColorTemplate.VORDIPLOM_COLORS;

    private SQLiteDatabase mDatabase;

    volatile boolean stopWorker;
    int readBufferPosition;
    byte readBuffer[];
    Thread workerThread;

    String receivedData;

    public Telemetria() {
        super();
        // Required empty public constructor
    }

    public static Telemetria newInstance(String param1, String param2) {
        Telemetria fragment = new Telemetria();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = new DB.GallandaBaseHelper(getContext()).getWritableDatabase();
        DataHandler = new Handler();
        DataHandler.postDelayed(DataRunnable, 2000);
    }

    Runnable DataRunnable = new Runnable() {
        @Override
        public void run() {
            if (getData) {
                //removeDataSet();
                //removeLastEntry();
                addEntry();
                //addDataSet();
                //Toast.makeText(getContext(), "Speed"+SpeedList.get(SpeedList.size())+", tmp"+TemperatureList.get(TemperatureList.size())+", bat"+BatteryList.get(BatteryList.size()), Toast.LENGTH_SHORT).show();
            }
            DataHandler.postDelayed(DataRunnable, 5500);
        }
    };

    /*public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        video = (VideoView) getView().findViewById(R.id.video);
        video.setVideoURI(Uri.parse("android.resource://com.example.izotz.agpr/"+R.raw.bigshaqfinal));
        video.start();
        video.requestFocus();
    }*/

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        locker = (ImageView) getView().findViewById(R.id.lockerTel);
        seeData = (ImageView) getView().findViewById(R.id.seeData);

        //threadData = new ThreadData(getContext());

        locker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                NonSwipeablePager.setPagingEnabled(!NonSwipeablePager.isPagingEnabled());
                v.setActivated(!v.isActivated());
                if (v.isActivated()) {
                    Toast.makeText(getContext(), "GetData", Toast.LENGTH_SHORT).show();
                    //new Thread((threadData)).start();
                } else {
                    Toast.makeText(getContext(), "StopGetData", Toast.LENGTH_SHORT).show();
                    //threadData.stopWorker = true;
                }
                getData = v.isActivated();
                return false;
            }
        });

        seeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), DataActivity.class);
                startActivity(i);
            }
        });

        lineChartVel = (LineChart) getView().findViewById(R.id.chartVel);
        txtVel = (TextView) getView().findViewById(R.id.txtVel);
        lineChartBat = (LineChart) getView().findViewById(R.id.chartBat);
        txtBat = (TextView) getView().findViewById(R.id.txtBat);
        lineChartTmp = (LineChart) getView().findViewById(R.id.chartTmp);
        txtTmp = (TextView) getView().findViewById(R.id.txtTmp);

        lineChartVel.setData(new LineData());
        lineChartBat.setData(new LineData());
        lineChartTmp.setData(new LineData());

        lineChartVel.invalidate();
        lineChartBat.invalidate();
        lineChartTmp.invalidate();
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        MainActivity.mConnectedThread.write(":0000000000F009000\n");
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = MainActivity.btSocket.getInputStream().available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            MainActivity.mConnectedThread.mmInStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            receivedData = data;
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private void getBluetoothData(){
        mConnectedThread.run();
    }

    private void addEntry() {

        //beginListenForData();
        //getBluetoothData();
        mConnectedThread.read();

        LineData dataVel = lineChartVel.getData();
        LineData dataTmp = lineChartBat.getData();
        LineData dataBat = lineChartTmp.getData();

        ILineDataSet setVel = dataVel.getDataSetByIndex(0);
        ILineDataSet setTmp = dataTmp.getDataSetByIndex(0);
        ILineDataSet setBat = dataBat.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (setVel == null) {
            setVel = createSetVel();
            dataVel.addDataSet(setVel);
        }
        if (setTmp == null) {
            setTmp = createSetTmp();
            dataTmp.addDataSet(setTmp);
        }
        if (setBat == null) {
            setBat = createSetBat();
            dataBat.addDataSet(setBat);
        }

        // choose a random dataSet
        int randomDataSetIndexVel = (int) (Math.random() * dataVel.getDataSetCount());
        //float yValueVel = SpeedList.get(SpeedList.size());
        float yValueVel = (float) (Math.random() * 10) + 50f;

        int randomDataSetIndexTmp = (int) (Math.random() * dataTmp.getDataSetCount());
        //float yValueTmp = TemperatureList.get(TemperatureList.size());
        float yValueTmp = (float) (Math.random() * 10) + 50f;

        int randomDataSetIndexBat = (int) (Math.random() * dataBat.getDataSetCount());
        //float yValueBat = BatteryList.get(BatteryList.size());
        float yValueBat = (float) (Math.random() * 10) + 50f;

        dataVel.addEntry(new Entry(dataVel.getDataSetByIndex(randomDataSetIndexVel).getEntryCount(), yValueVel), randomDataSetIndexVel);
        String yValueVelS = Integer.toString((int) yValueVel);
        txtVel.setText(yValueVelS);
        dataVel.notifyDataChanged();
        dataTmp.addEntry(new Entry(dataTmp.getDataSetByIndex(randomDataSetIndexTmp).getEntryCount(), yValueTmp), randomDataSetIndexTmp);
        String yValueTmpS = Integer.toString((int) yValueTmp);
        txtTmp.setText(yValueTmpS);
        dataTmp.notifyDataChanged();
        dataBat.addEntry(new Entry(dataBat.getDataSetByIndex(randomDataSetIndexBat).getEntryCount(), yValueBat), randomDataSetIndexBat);
        String yValueBatS = Integer.toString((int) yValueBat);
        txtBat.setText(yValueBatS);
        dataBat.notifyDataChanged();

        // let the chart know it's data has changed
        lineChartVel.notifyDataSetChanged();
        lineChartTmp.notifyDataSetChanged();
        lineChartBat.notifyDataSetChanged();

        lineChartVel.setVisibleXRangeMaximum(6);
        lineChartTmp.setVisibleXRangeMaximum(6);
        lineChartBat.setVisibleXRangeMaximum(6);
        //mChart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
//
//            // this automatically refreshes the chart (calls invalidate())
        lineChartVel.moveViewTo(dataVel.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        lineChartTmp.moveViewTo(dataTmp.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        lineChartBat.moveViewTo(dataBat.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);

        DB.DataObject currentDataObject = new DataObject((int) yValueVel, (int) yValueTmp, (int) yValueBat);
        ContentValues values = new ContentValues();
        values.put(GallandaDbSchema.DataTable.Cols.SPEED, currentDataObject.getSpeed());
        values.put(GallandaDbSchema.DataTable.Cols.TEMPERATURE, currentDataObject.getTemperature());
        values.put(GallandaDbSchema.DataTable.Cols.BATTERYCHARGE, currentDataObject.getBatterycharge());

        mDatabase.insert(GallandaDbSchema.DataTable.NAME, null, values);
    }

    private void removeLastEntry() {

        LineData dataVel = lineChartVel.getData();
        LineData dataTmp = lineChartTmp.getData();
        LineData dataBat = lineChartBat.getData();

        if (dataVel != null) {

            ILineDataSet set = dataVel.getDataSetByIndex(0);

            if (set != null) {

                Entry e = set.getEntryForXPos(set.getEntryCount() - 1, DataSet.Rounding.UP);

                dataVel.removeEntry(e, 0);
                // or remove by index
                // mData.removeEntryByXValue(xIndex, dataSetIndex);
                dataVel.notifyDataChanged();
                lineChartVel.notifyDataSetChanged();
                lineChartVel.invalidate();
            }
        }
        if (dataTmp != null) {

            ILineDataSet set = dataTmp.getDataSetByIndex(0);

            if (set != null) {

                //Entry e = set.getEntryForXValue(set.getEntryCount() - 1, Float.NaN);
                Entry e = set.getEntryForXPos(set.getEntryCount() - 1, DataSet.Rounding.UP);

                dataTmp.removeEntry(e, 0);
                // or remove by index
                // mData.removeEntryByXValue(xIndex, dataSetIndex);
                dataTmp.notifyDataChanged();
                lineChartTmp.notifyDataSetChanged();
                lineChartTmp.invalidate();
            }
        }
        if (dataBat != null) {

            ILineDataSet set = dataBat.getDataSetByIndex(0);

            if (set != null) {

                Entry e = set.getEntryForXPos(set.getEntryCount() - 1, DataSet.Rounding.UP);

                dataBat.removeEntry(e, 0);
                // or remove by index
                // mData.removeEntryByXValue(xIndex, dataSetIndex);
                dataBat.notifyDataChanged();
                lineChartBat.notifyDataSetChanged();
                lineChartBat.invalidate();
            }
        }
    }

    private void addDataSet() {

        LineData dataVel = lineChartVel.getData();
        LineData dataTmp = lineChartTmp.getData();
        LineData dataBat = lineChartBat.getData();

        if (dataVel != null) {

            int count = (dataVel.getDataSetCount() + 1);

            ArrayList<Entry> yVals = new ArrayList<Entry>();

            for (int i = 0; i < dataVel.getEntryCount(); i++) {
                yVals.add(new Entry(i, (float) (Math.random() * 50f) + 50f * count));
            }

            LineDataSet set = new LineDataSet(yVals, "DataSet " + count);
            set.setLineWidth(2.5f);
            set.setCircleRadius(4.5f);

            int color = mColors[count % mColors.length];

            set.setColor(color);
            set.setCircleColor(color);
            set.setHighLightColor(color);
            set.setValueTextSize(10f);
            set.setValueTextColor(color);

            dataVel.addDataSet(set);
            dataVel.notifyDataChanged();
            lineChartVel.notifyDataSetChanged();
            lineChartVel.invalidate();
        }
        if (dataTmp != null) {

            int count = (dataTmp.getDataSetCount() + 1);

            ArrayList<Entry> yVals = new ArrayList<Entry>();

            for (int i = 0; i < dataTmp.getEntryCount(); i++) {
                yVals.add(new Entry(i, (float) (Math.random() * 50f) + 50f * count));
            }

            LineDataSet set = new LineDataSet(yVals, "DataSet " + count);
            set.setLineWidth(2.5f);
            set.setCircleRadius(4.5f);

            int color = mColors[count % mColors.length];

            set.setColor(color);
            set.setCircleColor(color);
            set.setHighLightColor(color);
            set.setValueTextSize(10f);
            set.setValueTextColor(color);

            dataTmp.addDataSet(set);
            dataTmp.notifyDataChanged();
            lineChartTmp.notifyDataSetChanged();
            lineChartTmp.invalidate();
        }
        if (dataBat != null) {

            int count = (dataBat.getDataSetCount() + 1);

            ArrayList<Entry> yVals = new ArrayList<Entry>();

            for (int i = 0; i < dataBat.getEntryCount(); i++) {
                yVals.add(new Entry(i, (float) (Math.random() * 50f) + 50f * count));
            }

            LineDataSet set = new LineDataSet(yVals, "DataSet " + count);
            set.setLineWidth(2.5f);
            set.setCircleRadius(4.5f);

            int color = mColors[count % mColors.length];

            set.setColor(color);
            set.setCircleColor(color);
            set.setHighLightColor(color);
            set.setValueTextSize(10f);
            set.setValueTextColor(color);

            dataBat.addDataSet(set);
            dataBat.notifyDataChanged();
            lineChartBat.notifyDataSetChanged();
            lineChartBat.invalidate();
        }
    }

    private void removeDataSet() {

        LineData dataVel = lineChartVel.getData();
        LineData dataTmp = lineChartTmp.getData();
        LineData dataBat = lineChartBat.getData();

        if (dataVel != null) {

            dataVel.removeDataSet(dataVel.getDataSetByIndex(dataVel.getDataSetCount() - 1));

            lineChartVel.notifyDataSetChanged();
            lineChartVel.invalidate();
        }
        if (dataTmp != null) {

            dataTmp.removeDataSet(dataTmp.getDataSetByIndex(dataTmp.getDataSetCount() - 1));

            lineChartTmp.notifyDataSetChanged();
            lineChartTmp.invalidate();
        }
        if (dataBat != null) {

            dataBat.removeDataSet(dataBat.getDataSetByIndex(dataBat.getDataSetCount() - 1));

            lineChartBat.notifyDataSetChanged();
            lineChartBat.invalidate();
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionAddEntry:
                addEntry();
                Toast.makeText(this, "Entry added!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.actionRemoveEntry:
                removeLastEntry();
                Toast.makeText(this, "Entry removed!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.actionAddDataSet:
                addDataSet();
                Toast.makeText(this, "DataSet added!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.actionRemoveDataSet:
                removeDataSet();
                Toast.makeText(this, "DataSet removed!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.actionAddEmptyLineData:
                mChart.setData(new LineData());
                mChart.invalidate();
                Toast.makeText(this, "Empty data added!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.actionClear:
                mChart.clear();
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }*/

    private LineDataSet createSetVel() {

        LineDataSet set = new LineDataSet(null, "Speed DataSet");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(Color.rgb(0, 255, 0));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return set;
    }

    private LineDataSet createSetTmp() {

        LineDataSet set = new LineDataSet(null, "Temperature DataSet");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(Color.rgb(255, 10, 10));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return set;
    }

    private LineDataSet createSetBat() {

        LineDataSet set = new LineDataSet(null, "Battery DataSet");
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(Color.rgb(255, 254, 0));
        set.setCircleColor(Color.rgb(240, 99, 99));
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return set;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_telemetria, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
