package com.example.sasiroot.agpr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.AccessController;
import java.util.UUID;

import adapter.PagerAdapter;
import classes.Device;


public class MainActivity extends AppCompatActivity implements Autopilot.OnFragmentInteractionListener, MenuSlider.OnFragmentInteractionListener, Telemetria.OnFragmentInteractionListener {

    //////////////////////////////////////////////////////////////////BLUETOOTH//////////////////////////////////////////////////////////////////////////

    public static Handler bluetoothIn;

    final int handlerState = 0;                         //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    public static BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    public static ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    public static Device mCurrentDevice;
    private static String address = null;

    public static String speed, temperature, batteryCharge;

    ///////////////////////////////////////////////////////////////////BLUETOOTH//////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText("Autopilot"));
        tabLayout.addTab(tabLayout.newTab().setText("Menu"));
        tabLayout.addTab(tabLayout.newTab().setText("Telemetria"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setCurrentItem(1);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                        //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                    //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("\n");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        int dataLength = dataInPrint.length();                            //get length of data received

                        speed = recDataString.substring(0);

                        /*if (recDataString.charAt(0) == ':')								//if it starts with # we know it is what we are looking for
                        {
                            String speed = recDataString.substring(11, 12);
                            Telemetria.SpeedList.add(Float.parseFloat(speed));                             //get sensor value from string between indices 1-5
                            String temperature = recDataString.substring(13, 14);            //same again...
                            Telemetria.TemperatureList.add(Float.parseFloat(temperature));
                            String battery = recDataString.substring(15, 16);
                            Telemetria.BatteryList.add(Float.parseFloat(battery));
                        }*/
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        mCurrentDevice = (Device) intent.getSerializableExtra("CURRENT_DEVICE");
        address = mCurrentDevice.getIp();

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Errorea Socket.a sortzean", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
            mConnectedThread = new ConnectedThread(btSocket);
            //mConnectedThread.start();

            //I send a character when resuming.beginning transmission to check device is connected
            //If it is not an exception will be thrown in the write method and finish() will be called
            mConnectedThread.write("x");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                int i = 0;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Bluetooth no compatible", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    public class ConnectedThread extends Thread {
        public InputStream mmInStream = null;
        public OutputStream mmOutStream = null;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                mmInStream = btSocket.getInputStream();
                mmOutStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //mmInStream = tmpIn;
            // mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                /*try {
                    MainActivity.mConnectedThread.write(":0000000000F009000\n");
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }*/
            }


        }

        public void read() {
            try {
                char[] buffer = new char[1024 * 16];
                int bytes = 0;
                int bytesAvalaible;
                String line;
                MainActivity.mConnectedThread.write(":0000000000F009000\n");

                bytesAvalaible = mmInStream.available();
                if (bytesAvalaible>0) {
                    InputStreamReader reader = new InputStreamReader(mmInStream);
                    BufferedReader r = new BufferedReader(reader);
                    //while(-1 != (bytes = r.read(buffer))){

                    while ((line = r.readLine()) != null) {
                        Toast.makeText(MainActivity.this, line, Toast.LENGTH_SHORT).show();
                    }
                }
            /*bytesAvalaible = mmInStream.available();
            if (bytesAvalaible>0){
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                // Send the obtained bytes to the UI Activity via handler
                bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
            }*/
            } catch (IOException e) {
                Log.i("Custom", e.getMessage());
                e.printStackTrace();
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                //Thread.sleep(500);
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Ez da konexio bete", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
