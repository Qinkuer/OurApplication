package com.example.ourapplication;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class OrderFragment extends Fragment {
    private static final String TAG = "OrderFragment";
    private View  rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.order_fragment_layout, container, false);


        return rootView;
    }
}
