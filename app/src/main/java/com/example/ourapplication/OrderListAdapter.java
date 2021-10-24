package com.example.ourapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Timestamp;
import java.util.Date;

public class OrderListAdapter  extends ArrayAdapter<OrderInf_Item_ForAdapter> {
    private static final String TAG = "OrderListAdapter";
    private Context mContext;
    private int resourceID;
    public OrderListAdapter(@NonNull Context context, int resource) {
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
            vh.tvDetailAddress=view.findViewById(R.id.textView_textView_item_order_address);
            vh.tvEndTime=view.findViewById(R.id.tv_Order_Item_End_Time);
            vh.buttonLeadTheWay=view.findViewById(R.id.button_lead_the_way);

            vh.tvDetailAddress.setText(getItem(position).detailedAddress);
            vh.tvEndTime.setText(getItem(position).endTime.toString());
//            Log.e(TAG,getItem(position).endTime.toString());
            initOnClick(vh);
            CheckNowTimeAndEndTime(getItem(position),vh);
            view.setTag(vh);

        }
        else{
            view=convertView;
            CheckNowTimeAndEndTime(getItem(position),(ViewHolder) view.getTag());
        }


        return view;
    }

    private void initOnClick(ViewHolder vh){
        vh.buttonLeadTheWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
            }
        });
    }


    //判断是否到了结束时间,如果已经到了结束时间 就不能点击按钮
    private void CheckNowTimeAndEndTime(OrderInf_Item_ForAdapter oiif,ViewHolder vh){
        Timestamp Nowtime=new Timestamp((new Date()).getTime());
        if(Nowtime.getTime()-oiif.endTime.getTime()>-100){
            vh.buttonLeadTheWay.setEnabled(false);
        }

    }


    class ViewHolder{
        TextView tvDetailAddress;
        TextView tvEndTime;
        Button buttonLeadTheWay;
    }
}
