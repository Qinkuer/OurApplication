package com.example.ourapplication;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class LeaseFragment extends Fragment {
    private static final String TAG = "LeasseFragment";
    private String UserName_HavedLoggedIn=null;


    public LeaseFragment(String UserName){
        super();
        this.UserName_HavedLoggedIn=UserName;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.lease_fragment_layout, container, false);

        return rootView;
    }
}
