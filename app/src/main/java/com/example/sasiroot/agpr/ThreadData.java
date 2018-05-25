package com.example.sasiroot.agpr;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sasiroot on 4/13/18.
 */

public class ThreadData extends Thread {

    List<String> SpeedList = new ArrayList<String>();
    List<String> TemperatureList = new ArrayList<String>();
    List<String> BatteryList = new ArrayList<String>();

    final Handler handler = new Handler();
    final byte delimiter = 10; //This is the ASCII code for a newline character

    Context context;
    BluetoothSocket mmSocket = MainActivity.btSocket;
    InputStream mmInputStream;
    boolean stopWorker = false;
    int readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];

    public ThreadData(Context context) {
        this.context = context;
        mmSocket = MainActivity.btSocket;
        try {
            mmInputStream = mmSocket.getInputStream();
        } catch (IOException e) {
        }
    }

    public void run() {
        while (!stopWorker) {
            /*try {
                MainActivity.mConnectedThread.write(":0000000000F009000\n");
                int bytesAvailable = mmInputStream.available();
                if (bytesAvailable > 0) {
                    byte[] packetBytes = new byte[bytesAvailable];
                    mmInputStream.read(packetBytes);
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];
                        if (b == delimiter) {
                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                            final String data = new String(encodedBytes, "US-ASCII");
                            readBufferPosition = 0;

                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            readBuffer[readBufferPosition++] = b;
                        }
                    }
                }
            } catch (IOException ex) {
                stopWorker = true;
            }*/
        }
    }
}