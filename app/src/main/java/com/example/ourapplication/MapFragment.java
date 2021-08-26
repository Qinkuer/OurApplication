package com.example.ourapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.map_fragment_layout, container, false);
        TextView tx=rootView.findViewById(R.id.textTest1);
        tx.setText("安康士大夫立刻就受到了看风景卢卡斯大家");
        return rootView;
    }
}
