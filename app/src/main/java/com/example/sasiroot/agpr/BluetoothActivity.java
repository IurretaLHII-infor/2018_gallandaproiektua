package com.example.sasiroot.agpr;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import adapter.DeviceAdapter;
import classes.Device;

public class BluetoothActivity extends AppCompatActivity {
    // Debugging for LOGCAT
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // declare button for launching website and textview for connection status
    private TextView connecting;

    // EXTRA string to send on to mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static Device CURRENT_DEVICE = new Device();

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private DeviceAdapter mPairedDevicesDeviceAdapter;
    private ArrayList<Device> DeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        checkBTState();
        AddArrayDevices();
        connecting = (TextView) findViewById(R.id.connecting);
        connecting.setTextSize(40);
        connecting.setText(" ");

    }

    @Override
    public void onResume()
    {
        super.onResume();
        //***************

        checkBTState();
        if (DeviceList.size() == 0) {
            AddArrayDevices();
        }
    }

    public void AddArrayDevices(){
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Add previosuly paired devices to the array
        if (pairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
            Device newDevice;
            for (BluetoothDevice device : pairedDevices) {
                //mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                newDevice = new Device(getBTMajorDeviceClass(device.getBluetoothClass().getMajorDeviceClass()), device.getAddress(), device.getName());
                DeviceList.add(newDevice);
            }
        } else {
            /*Device noDevices = new Device("unknown!", "00:00:00:00:00:00", null);
            DeviceList.add(noDevices);*/
        }

        // Initialize array adapter for paired devices
        mPairedDevicesDeviceAdapter = new DeviceAdapter(BluetoothActivity.this, DeviceList);

        // Find and set up the ListView for paired devices
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesDeviceAdapter);
        //pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(BluetoothActivity.this, MainActivity.class);
                i.putExtra("CURRENT_DEVICE", DeviceList.get(position));
                startActivity(i);
            }
        });
        //pairedListView.setOnItemSelectedListener(mDeviceSelectedListener);
    }

    protected ListView pairedListView;

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------
    /*// Set up on-click listener for the list (nicked this - unsure)
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            connecting.setText("Conectando...");

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Device d = (Device) BluetoothActivity.this.pairedListView.getSelectedItem();
            int i = 0;

            Intent i = new Intent(BluetoothActivity.this, MainActivity.class);
            i.putExtra(CURRENT_DEVICE, d);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);


            /*String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Toast.makeText(BluetoothActivity.this, address, Toast.LENGTH_SHORT).show();

            // Make an intent to start next activity while taking an extra which is the MAC address.
            Intent i = new Intent(BluetoothActivity.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };*/

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter=BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private String getBTMajorDeviceClass(int major) {
        switch (major) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTER";
            case BluetoothClass.Device.Major.HEALTH:
                return "HEALTH";
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGING";
            case BluetoothClass.Device.Major.MISC:
                return "MISC";
            case BluetoothClass.Device.Major.NETWORKING:
                return "NETWORKING";
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIPHERAL";
            case BluetoothClass.Device.Major.PHONE:
                return "PHONE";
            case BluetoothClass.Device.Major.TOY:
                return "TOY";
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "UNCATEGORIZED";
            case BluetoothClass.Device.Major.WEARABLE:
                return "AUDIO_VIDEO";
            default:
                return "unknown!";
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh:
                DeviceList.removeAll(DeviceList);
                AddArrayDevices();
                Toast.makeText(BluetoothActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
