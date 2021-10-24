package com.example.ourapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Timestamp;
import java.util.Date;


public class LeaseParkingsAdapter extends ArrayAdapter<LeaseParkingInf_Item_ForAdapter>{
    private static final String TAG = "LeaseParkingsAdapter";
    private Context mContext;
    private int resourceID;
    public LeaseParkingsAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.mContext=context;
        this.resourceID=resource;
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        final ViewHolder vh;
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceID,parent,false );
            vh = new ViewHolder();
            vh.tvDetailAddress=view.findViewById(R.id.textView_LeaseItem_DetailAddress);
            vh.tvEndTime=view.findViewById(R.id.textView_LeaseItem_EndTime);
            vh.tvStartTime=view.findViewById(R.id.textView_LeaseItem_StartTime);
            vh.tvParkingState=view.findViewById(R.id.textView_StateOfParking);

            vh.tvDetailAddress.setText(getItem(position).detailedAddress);
            vh.tvStartTime.setText(getItem(position).startTime.toString());
            vh.tvEndTime.setText(getItem(position).endTime.toString());
            CheakEndTime(getItem(position),vh);
//            Log.e(TAG,getItem(position).endTime.toString());
            view.setTag(vh);
        }
        else{
            view=convertView;
            CheakEndTime(getItem(position),(ViewHolder)view.getTag());
        }


        return view;
    }

    //检查结束时间,改变显示状态
    @SuppressLint("ResourceAsColor")
    private void CheakEndTime(LeaseParkingInf_Item_ForAdapter lpiifa , ViewHolder vh){
        Timestamp Nowtime=new Timestamp((new Date()).getTime());
        if(Nowtime.getTime()-lpiifa.endTime.getTime()>-100){
            vh.tvParkingState.setText("完成");
            vh.tvParkingState.setTextColor(Color.GREEN);
        }
        else {
            vh.tvParkingState.setText("占用");
            vh.tvParkingState.setTextColor(Color.YELLOW);
        }



    }


    class ViewHolder{
        TextView tvDetailAddress;
        TextView tvStartTime;
        TextView tvEndTime;
        TextView tvParkingState;
    }
}