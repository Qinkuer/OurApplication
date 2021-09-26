package com.example.ourapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CarNumberStringAdapter extends ArrayAdapter<String> {
    private static final String TAG = "CarNumberStringAdapter";
    private List<String> CarNumberString;
    private Context mContext;
    private int resourceId;

    public CarNumberStringAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.mContext=context;
        this.resourceId=resource;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String CN_String=getItem(position);
        View view;
        final ViewHolder vh;
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false );
            vh = new ViewHolder();
            vh.tvCarNumberSting=view.findViewById(R.id.textView_CarNumber_String);
            view.setTag(vh);
        }
        else{
            view=convertView;
            vh = (ViewHolder) view.getTag();
        }
        vh.tvCarNumberSting.setText(CN_String);

        Log.e(TAG,"生成item的View");
        return view;
    }

    class ViewHolder{
        TextView tvCarNumberSting;
    }
}
