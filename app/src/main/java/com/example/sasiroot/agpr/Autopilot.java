package com.example.sasiroot.agpr;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Autopilot extends Fragment {

    protected static final int FLAG_LIGHT_EMERGENCY = 0;
    protected static final int FLAG_POSITION_LIGHTS = 1;
    protected static final int FLAG_SHORT_LIGHTS = 2;
    protected static final int FLAG_LONG_LIGHTS = 3;
    protected static final int FLAG_BREAKING_LIGHTS = 4;
    protected static final int FLAG_INTERMITTENT_LEFT = 5;
    protected static final int FLAG_INTERMITTENT_RIGHT = 6;
    protected static final int FLAG_CLAXON = 7;
    protected static final int FLAG_WINDSCREEN = 8;
    protected static final int FLAG_F_B = 10;
    protected static final int FLAG_SPEED = 11;
    protected static final int FLAG_DIRECTION = 13;


    private ImageView locker;
    private OnFragmentInteractionListener mListener;
    private AlphaAnimation animation;
    private ImageView shortligths;
    private ImageView longlights;
    private ImageView left;
    private ImageView right;
    private ImageView emergency;
    private ImageView wipers;
    private ImageView claxon;
    private TextView activated;
    private BluetoothDevice device;
    private TextView acelerate;
    private TextView brake;
    private int speed;
    private int angle;
    private ImageView goright;
    private ImageView goleft;
    private ImageView fb;
    private String direction;

    protected SensorEventListener proximityListener;
    protected SensorEventListener gyroscopeListener;
    protected SensorManager sm;
    protected Sensor gyroscope;
    protected Sensor proximity;



    public Autopilot() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        locker = (ImageView) getView().findViewById(R.id.locker);
        shortligths = (ImageView) getView().findViewById(R.id.shortlights);
        longlights = (ImageView) getView().findViewById(R.id.longligths);
        left = (ImageView) getView().findViewById(R.id.left);
        right = (ImageView) getView().findViewById(R.id.right);
        emergency = (ImageView) getView().findViewById(R.id.emergency);
        wipers = (ImageView) getView().findViewById(R.id.wipers);
        claxon = (ImageView) getView().findViewById(R.id.claxon);
        activated = (TextView) getView().findViewById(R.id.activated);
        acelerate = (TextView) getView().findViewById(R.id.acelerator);
        brake = (TextView) getView().findViewById(R.id.brake);
        goright = (ImageView) getView().findViewById(R.id.goright);
        goleft = (ImageView) getView().findViewById(R.id.goleft);
        fb = (ImageView) getView().findViewById(R.id.fb);

        shortligths.setEnabled(false);
        longlights.setEnabled(false);
        left.setEnabled(false);
        right.setEnabled(false);
        emergency.setEnabled(false);
        wipers.setEnabled(false);
        claxon.setEnabled(false);
        brake.setEnabled(false);
        acelerate.setEnabled(false);
        goright.setEnabled(false);
        goleft.setEnabled(false);
        fb.setEnabled(false);

        direction = "f";
        speed = 0;
        angle=144;

        animation = new AlphaAnimation(1,0);
        animation.setDuration(250);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);


        locker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                NonSwipeablePager.setPagingEnabled(!NonSwipeablePager.isPagingEnabled());
                v.setActivated(!v.isActivated());
                if(v.isActivated()){
                    activated.setVisibility(View.VISIBLE);
                    activated.startAnimation(AnimationUtils.loadAnimation(getActivity().getBaseContext(),android.R.anim.slide_in_left));
                    sendSignal();
                    fb.setRotation(0);
                    shortligths.setEnabled(true);
                    longlights.setEnabled(true);
                    left.setEnabled(true);
                    right.setEnabled(true);
                    emergency.setEnabled(true);
                    wipers.setEnabled(true);
                    claxon.setEnabled(true);
                    acelerate.setEnabled(true);
                    brake.setEnabled(true);
                    goleft.setEnabled(true);
                    goright.setEnabled(true);
                    fb.setEnabled(true);
                }if(!v.isActivated()){
                    activated.setVisibility(View.INVISIBLE);
                    shortligths.setEnabled(false);
                    longlights.setEnabled(false);
                    left.setEnabled(false);
                    right.setEnabled(false);
                    emergency.setEnabled(false);
                    wipers.setEnabled(false);
                    claxon.setEnabled(false);
                    acelerate.setEnabled(false);
                    brake.setEnabled(false);
                    goleft.setEnabled(false);
                    goright.setEnabled(false);
                    fb.setEnabled(false);
                    right.clearAnimation();
                    emergency.clearAnimation();
                    left.clearAnimation();
                }
                return false;
            }
        });


        shortligths.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (v.isActivated()) {
                    buildSignal(Autopilot.FLAG_SHORT_LIGHTS);
                    if (sendSignal()) {
                        v.setActivated(false);
                    }
                }
                else {
                    buildSignal(Autopilot.FLAG_SHORT_LIGHTS);
                    if (longlights.isActivated()) {
                        buildSignal(Autopilot.FLAG_LONG_LIGHTS);
                    }
                    if (sendSignal()) {
                        v.setActivated(true);
                        if (longlights.isActivated()) {
                            longlights.setActivated(false);
                        }
                    }
                }
                return false;
            }
        });
        longlights.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (v.isActivated()) {
                    buildSignal(Autopilot.FLAG_LONG_LIGHTS);
                    if (sendSignal()) {
                        v.setActivated(false);
                    }
                }
                else {
                    buildSignal(Autopilot.FLAG_LONG_LIGHTS);
                    if (shortligths.isActivated()) {
                        buildSignal(Autopilot.FLAG_SHORT_LIGHTS);
                    }
                    if (sendSignal()) {
                        v.setActivated(true);
                        if (shortligths.isActivated()) {
                            shortligths.setActivated(false);
                        }
                    }
                }

                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                right.setActivated(false);
                emergency.setActivated(false);
                right.clearAnimation();
                emergency.clearAnimation();
                v.setActivated(!v.isActivated());
                if(v.isActivated()){
                    left.startAnimation(animation);
                }
                if(!v.isActivated()){
                    left.clearAnimation();
                }

                if (v.isActivated()) {
                    buildSignal(Autopilot.FLAG_INTERMITTENT_LEFT);
                    if (sendSignal()) {
                        // Autopilot.this.clearButtonAnimation((ImageButton) v);
                    }
                }
                else {
                    buildSignal(Autopilot.FLAG_INTERMITTENT_LEFT);
                    if (Autopilot.this.right.isActivated()) {
                        buildSignal(Autopilot.FLAG_INTERMITTENT_RIGHT);
                    }
                    if (sendSignal()) {
                        if (Autopilot.this.right.isActivated()) {
                            // Autopilot.this.clearButtonAnimation(Autopilot.this.right);
                        }
                        //Autopilot.this.startButtonAnimation((ImageButton) v, Autopilot.FLAG_INTERMITTENT_LEFT);
                    }

                }

                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                left.setActivated(false);
                emergency.setActivated(false);
                left.clearAnimation();
                emergency.clearAnimation();
                v.setActivated(!v.isActivated());
                if(v.isActivated()){
                    right.startAnimation(animation);
                }
                if(!v.isActivated()){
                    right.clearAnimation();
                }


                if (v.isActivated()) {
                    buildSignal(Autopilot.FLAG_INTERMITTENT_RIGHT);
                    if (sendSignal()) {
                        //Autopilot.this.clearButtonAnimation((ImageButton) v);
                    }
                }
                else {
                    buildSignal(Autopilot.FLAG_INTERMITTENT_RIGHT);
                    if (Autopilot.this.left.isActivated()) {
                        buildSignal(Autopilot.FLAG_INTERMITTENT_LEFT);
                    }
                    if (sendSignal()) {
                        if (Autopilot.this.left.isActivated()) {
                            // Autopilot.this.clearButtonAnimation(Autopilot.this.left);
                        }
                        //Autopilot.this.startButtonAnimation((ImageButton) v, Autopilot.FLAG_INTERMITTENT_RIGHT);
                    }

                }


                return false;
            }
        });
        emergency.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setActivated(!v.isActivated());
                if(v.isActivated()){

                    right.startAnimation(animation);
                    left.startAnimation(animation);
                    emergency.startAnimation(animation);

                    right.setActivated(false);
                    left.setActivated(false);

                    buildSignal(Autopilot.FLAG_LIGHT_EMERGENCY);

                    if (sendSignal()) {
                        // if (left.isActivated()) Autopilot.this.clearButtonAnimation(left);
                        // if (right.isActivated()) Autopilot.this.clearButtonAnimation(right);
                    } }
                if(!v.isActivated()){
                    emergency.clearAnimation();
                    right.clearAnimation();
                    left.clearAnimation();

                    right.setActivated(true);
                    left.setActivated(true);

                    buildSignal(Autopilot.FLAG_LIGHT_EMERGENCY);
                    if (sendSignal()) {

                    }
                }



                return false;
            }
        });
        wipers.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (v.isActivated()) {
                    buildSignal(Autopilot.FLAG_WINDSCREEN);
                    if (sendSignal()) {
                        v.setActivated(false);
                    }
                }
                else {
                    buildSignal(Autopilot.FLAG_WINDSCREEN);
                    if (sendSignal()) {
                        v.setActivated(true);
                    }
                }

                return false;
            }
        });
        claxon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: //Begins signal
                        //OnTouchListener implementation forces us to define the pressed state
                        //programatically
                        buildSignal(Autopilot.FLAG_CLAXON);
                        if (sendSignal()) {
                            v.setPressed(true);
                        }
                        break;
                    case MotionEvent.ACTION_UP: //Ends signal
                        //OnTouchListener implementation forces us to define the pressed state
                        //programatically
                        buildSignal(Autopilot.FLAG_CLAXON);
                        if (sendSignal()) {
                            v.setPressed(false);
                        }
                        break;
                }
                return true;
            }
        });

        acelerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed = speed + 10;
                if(speed >= 99){
                    speed = 90;
                }
                buildSpeedSignal(Autopilot.FLAG_SPEED);
                if (sendSignal()) {
                    v.setPressed(true);
                }
            }
        });

        brake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed = speed -10;
                if(speed >= 0){
                    speed = 0;
                }
                buildSignal(Autopilot.FLAG_BREAKING_LIGHTS);
                buildSpeedSignal(Autopilot.FLAG_SPEED);
                if (sendSignal()) {
                    v.setPressed(true);
                }
            }
        });

        goright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle = angle + 60;

                if(angle > 255){
                    angle=255;
                }

                buildAngleSignal(Autopilot.FLAG_DIRECTION);
                if (sendSignal()) {
                    v.setPressed(true);
                }

            }
        });

        goleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle = angle - 60;
                if(angle < 16){
                    angle=16;
                }

                buildAngleSignal(Autopilot.FLAG_DIRECTION);
                if (sendSignal()) {
                    v.setPressed(true);
                }


            }
        });

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(direction=="f"){

                    direction="b";
                    buildfbSignal(Autopilot.FLAG_F_B);
                    sendSignal();
                    fb.setRotation(180);
                }
                else{

                    direction="f";
                    buildfbSignal(Autopilot.FLAG_F_B);
                    sendSignal();
                    fb.setRotation(0);
                }

            }
        });


    }

    protected String message = ":0000000000F009000\n";
    final int handlerState = 0;
    protected Handler bluetoothIn; //used to identify handler message

    private StringBuilder recDataString = new StringBuilder();


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;



    protected void buildSignal(int position) {
        position = position +1; //fixme: Configure flag constants

        if (this.message.charAt(position)=='0') {
            this.message = this.message.substring(0, position) + '1' + this.message.substring(position+1);
        }
        else {
            this.message = this.message.substring(0, position) + '0' + this.message.substring(position+1);
        }
    }
    protected void buildfbSignal(int position) {
        position = position +1; //fixme: Configure flag constants

        if (this.message.charAt(position)=='f') {
            this.message = this.message.substring(0, position) + 'b' + this.message.substring(position+1);
        }
        else {
            this.message = this.message.substring(0, position) + 'f' + this.message.substring(position+1);
        }
    }

    protected void buildSpeedSignal(int position) {
        position = position +1; //fixme: Configure flag constants

        String stringSpeedOSoundOSonic;

        if (speed < 16){
            stringSpeedOSoundOSonic ="10";
        }
        else if (speed  > 90){
            stringSpeedOSoundOSonic = "90";
        }
        else {
            stringSpeedOSoundOSonic = Integer.toString(speed);
        }

        this.message = this.message.substring(0, position) + stringSpeedOSoundOSonic + this.message.substring(position+2);

    }

    protected void buildAngleSignal(int position) {
        position = position +1; //fixme: Configure flag constants



        String anglestring = Integer.toHexString(angle);

        if (angle < 10){
            anglestring ="0A";
        }
        else if (angle >= 255){
            anglestring = "FF";
        }

        this.message = this.message.substring(0, position) + anglestring + this.message.substring(position+2);

    }


    protected boolean sendSignal() {

        MainActivity.mConnectedThread.write(this.message);

        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_autopilot, container, false);

    }

    // TODO: Rename method, update argument and hook method into UI event
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
