package com.example.sasiroot.agpr;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.Format;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import DB.GallandaDbSchema;
import classes.FromDataObject;

public class DataActivity extends AppCompatActivity {

    private CalendarView startCalendar;
    private CalendarView endCalendar;

    private TextView minSpeed;
    private TextView maxSpeed;
    private TextView avgSpeed;
    private TextView minTemperature;
    private TextView maxTemperature;
    private TextView avgTemperature;

    private SQLiteDatabase mDatabase;

    private String startCalendarYear;
    private String startCalendarMonth;
    private String startCalendarDayOfMonth;

    private String endCalendarYear;
    private String endCalendarMonth;
    private String endCalendarDayOfMonth;

    private static final SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat formatMonth = new SimpleDateFormat("MM");
    private static final SimpleDateFormat formatDayOfMonth = new SimpleDateFormat("dd");

    private static final SimpleDateFormat formatMonthDay = new SimpleDateFormat("dd");

    private String sqlQueryEach;
    private String sqlQueryOne;

    private ArrayList<FromDataObject> FromDataList = new ArrayList<>();

    private static final int ID_COLUMN = 0;
    private static final int TIMESTAMP_COLUMN = 1;
    private static final int SPEED_COLUMN = 2;
    private static final int TEMPERATURE_COLUMN = 3;
    private static final int BATTERY_COLUMN = 4;

    private static final int AVG_SPEED = 0;
    private static final int MIN_SPEED = 2;
    private static final int MAX_SPEED = 1;
    private static final int AVG_TEMPERATURE = 3;
    private static final int MIN_TEMPERATURE = 5;
    private static final int MAX_TEMPERATURE = 4;

    private LineChart lineChartVel;
    private LineChart lineChartBat;
    private LineChart lineChartTmp;

    private LineData dataVel;
    private LineData dataTmp;
    private LineData dataBat;

    private ILineDataSet setVel;
    private ILineDataSet setTmp;
    private ILineDataSet setBat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        mDatabase = new DB.GallandaBaseHelper(DataActivity.this).getReadableDatabase();

        startCalendar = (CalendarView) findViewById(R.id.startCalendar);
        endCalendar = (CalendarView) findViewById(R.id.endCalendar);

        minSpeed = (TextView) findViewById(R.id.dataSpeedMin);
        maxSpeed = (TextView) findViewById(R.id.dataSpeedMax);
        avgSpeed = (TextView) findViewById(R.id.dataSpeedAVG);
        minTemperature = (TextView) findViewById(R.id.dataTmpMin);
        maxTemperature = (TextView) findViewById(R.id.dataTmpMax);
        avgTemperature = (TextView) findViewById(R.id.dataTmpAVG);

        startCalendarDayOfMonth = formatDayOfMonth.format(startCalendar.getDate());
        startCalendarMonth = formatMonth.format(startCalendar.getDate());
        startCalendarYear = formatYear.format(startCalendar.getDate());

        endCalendarDayOfMonth = formatDayOfMonth.format(endCalendar.getDate());
        endCalendarMonth = formatMonth.format(endCalendar.getDate());
        endCalendarYear = formatYear.format(endCalendar.getDate());

        lineChartVel = (LineChart) findViewById(R.id.dataSpeedChart);
        lineChartTmp = (LineChart) findViewById(R.id.dataTemperatureChart);
        lineChartBat = (LineChart) findViewById(R.id.dataBatteryChart);

        lineChartVel.setData(new LineData());
        lineChartBat.setData(new LineData());
        lineChartTmp.setData(new LineData());

        lineChartVel.setDescription("Speed Chart");
        lineChartVel.setDescriptionTextSize(30);
        lineChartTmp.setDescription("Temperature Chart");
        lineChartTmp.setDescriptionTextSize(30);
        lineChartBat.setDescription("Battery Charge Chart");
        lineChartBat.setDescriptionTextSize(30);

        lineChartVel.setDrawGridBackground(false);
        lineChartTmp.setDrawGridBackground(false);
        lineChartBat.setDrawGridBackground(false);

        lineChartVel.setMaxVisibleValueCount(200);
        lineChartVel.setDrawMarkerViews(true);
        lineChartTmp.setMaxVisibleValueCount(200);
        lineChartTmp.setDrawMarkerViews(true);
        lineChartBat.setMaxVisibleValueCount(200);
        lineChartBat.setDrawMarkerViews(true);

        lineChartVel.invalidate();
        lineChartBat.invalidate();
        lineChartTmp.invalidate();

        startCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                startCalendarYear =  String.format("%02d", year);
                startCalendarMonth = String.format("%02d", month + 1);
                startCalendarDayOfMonth = String.format("%02d", dayOfMonth);

                executeSQL();
            }
        });

        endCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                endCalendarYear = String.format("%02d", year);
                endCalendarMonth = String.format("%02d", month + 1);
                endCalendarDayOfMonth = String.format("%02d", dayOfMonth);

                executeSQL();
            }
        });
    }

    private void executeSQL(){
        FromDataList.clear();

        updateSQLString();
        Cursor cEach = mDatabase.rawQuery(sqlQueryEach, null);
        cEach.moveToFirst();

        while (cEach.moveToNext()){
            FromDataObject helpObject = new FromDataObject();
            helpObject.setId(cEach.getInt(ID_COLUMN));
            helpObject.setTimestanp(cEach.getString(TIMESTAMP_COLUMN));
            helpObject.setSpeed(cEach.getInt(SPEED_COLUMN));
            helpObject.setTemperature(cEach.getInt(TEMPERATURE_COLUMN));
            helpObject.setBatteryCharge(cEach.getInt(BATTERY_COLUMN));
            FromDataList.add(helpObject);
            cEach.moveToNext();
        }

        updateChart();

        Cursor cOne = mDatabase.rawQuery(sqlQueryOne, null);
        cOne.moveToFirst();

        avgSpeed.setText(Integer.toString(cOne.getInt(AVG_SPEED)));
        minSpeed.setText(Integer.toString(cOne.getInt(MIN_SPEED)));
        maxSpeed.setText(Integer.toString(cOne.getInt(MAX_SPEED)));
        avgTemperature.setText(Integer.toString(cOne.getInt(AVG_TEMPERATURE)));
        minTemperature.setText(Integer.toString(cOne.getInt(MIN_TEMPERATURE)));
        maxTemperature.setText(Integer.toString(cOne.getInt(MAX_TEMPERATURE)));

    }

    private void updateSQLString(){
        sqlQueryEach = "SELECT * FROM data WHERE timestamp between '" + startCalendarYear
                + "-" + startCalendarMonth + "-" + startCalendarDayOfMonth + "' and '"
                + endCalendarYear + "-" + endCalendarMonth + "-" + endCalendarDayOfMonth + "';";
        sqlQueryOne = "SELECT avg(speed), max(speed), min(speed), avg(temperature), max(temperature), min(temperature) FROM data WHERE timestamp between '" + startCalendarYear
                + "-" + startCalendarMonth + "-" + startCalendarDayOfMonth + "' and '"
                + endCalendarYear + "-" + endCalendarMonth + "-" + endCalendarDayOfMonth + "';";
    }

    private void updateChart(){
        removeDataSet();
        for (int i=0; i<FromDataList.size(); i++) {
            addEntry(FromDataList.get(i), i);
        }
    }

    private float createTimestamp(FromDataObject object){
        String timestampString = object.getTimestanp().substring(9, 10)
                + object.getTimestanp().substring(11, 13)
                + object.getTimestanp().substring(14, 16) + object.getTimestanp().substring(17, 19);
        float timestamp = Long.parseLong(timestampString);
        return timestamp;
    }

    private void addEntry(FromDataObject object, int pos){

        dataVel = new LineData();
        dataTmp = new LineData();
        dataBat = new LineData();

        dataVel = lineChartVel.getData();
        dataTmp = lineChartBat.getData();
        dataBat = lineChartTmp.getData();

        setVel = dataVel.getDataSetByIndex(0);
        setTmp = dataTmp.getDataSetByIndex(0);
        setBat = dataBat.getDataSetByIndex(0);

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

        float timestamp = createTimestamp(object);

        int SpdValue = object.getSpeed();
        int TmpValue = object.getTemperature();
        int BatValue = object.getBatteryCharge();

        dataVel.addEntry(new Entry(pos, SpdValue), 0);
        dataVel.notifyDataChanged();
        dataTmp.addEntry(new Entry(pos, TmpValue), 0);
        dataTmp.notifyDataChanged();
        dataBat.addEntry(new Entry(pos, BatValue), 0);
        dataBat.notifyDataChanged();

        lineChartVel.notifyDataSetChanged();
        lineChartTmp.notifyDataSetChanged();
        lineChartBat.notifyDataSetChanged();

        lineChartVel.setVisibleXRangeMaximum(12);
        lineChartTmp.setVisibleXRangeMaximum(12);
        lineChartBat.setVisibleXRangeMaximum(12);

        lineChartVel.moveViewTo(dataVel.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        lineChartTmp.moveViewTo(dataTmp.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        lineChartBat.moveViewTo(dataBat.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.data_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.close:
                DataActivity.this.finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
