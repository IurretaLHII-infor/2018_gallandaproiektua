package com.example.sasiroot.agpr;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import classes.Device;

public class MenuSlider extends Fragment {
    private CoordinatorLayout coordinatorLayout;
    private ImageView left;
    private ImageView right;
    private ImageView info;

    private OnFragmentInteractionListener mListener;

    private Device device;

    public MenuSlider() {
        // Required empty public constructor
    }


    public static MenuSlider newInstance(String param1, String param2) {
        MenuSlider fragment = new MenuSlider();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //device = MainActivity.getDevice();
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id
                .coordinatorLayout);
        info = (ImageView) getView().findViewById(R.id.info);

        info.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Name: " + MainActivity.mCurrentDevice.getName()+"- Ip: " + MainActivity.mCurrentDevice.getIp(), Snackbar.LENGTH_LONG);
                TextView mainTextView = (TextView) (snackbar.getView()).findViewById(android.support.design.R.id.snackbar_text);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    mainTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                else
                    mainTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                mainTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                snackbar.show();
                return false;
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_slider, container, false);
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
