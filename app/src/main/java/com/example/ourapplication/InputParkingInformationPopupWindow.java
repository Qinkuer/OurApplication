package com.example.ourapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InputParkingInformationPopupWindow extends PopupWindow {
    private static final String TAG = "PopupWindow";
    private Context mContext;
    private View mView;
    private Button bottonCancle=null;
    private Button bottonSubmit=null;
    private TextView textViewParkingEndTime=null;
    private TextView textViewAddressInformation=null;
    private TimePickerView pvTime;
    public InputParkingInformationPopupWindow(Activity context){
        this.mContext=context;
        this.mView= LayoutInflater.from(mContext).inflate(R.layout.layout_complete_parking_information,null);

        init();

    }
    private void init(){

        // 设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        this.mView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mView.findViewById(R.id.constraintLayoutMain0).getTop();

                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.mView);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //marker地址信息栏
        textViewAddressInformation=mView.findViewById(R.id.textView_address);

//        创建时间选择器
        CreateSelectTime();

        bottonCancle=mView.findViewById(R.id.buttonCompleteParkingInformation_Cancel);
        bottonSubmit=mView.findViewById(R.id.buttonCompleteParkingInformation_submit);
        bottonCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        bottonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        textViewParkingEndTime=mView.findViewById(R.id.textView_parking_end_time);
        textViewParkingEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTime.show();
            }
        });
    }

    private void CreateSelectTime(){
        //时间选择器
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar endDate = Calendar.getInstance();//最长的时间
        endDate.set(2025, 11, 31);
        pvTime = new TimePickerBuilder(getContentView().getContext(), new OnTimeSelectListener() {
            //确认选择日期
            @Override
            public void onTimeSelect(Date date, View v) {

//                Toast.makeText(getContentView().getContext(), getTime(date), Toast.LENGTH_SHORT).show();
                textViewParkingEndTime.setText(getTime(date));
            }
        }).setType(new boolean[]{true, true, true, true, false, false})
                // 默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确认")//确认按钮文字
                .setTitleSize(20)//标题文字大小
                .setTitleText("选择时间")//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(false)//是否循环滚动
                .setRangDate(selectedDate,endDate)//起始终止年月日设定
                .setLabel("年","月","日","时","分","秒")//默认设置为年月日时分秒
//                将TimePickerBuilder改成对话框形式,就可以避免被PopupWindow覆盖
                .isDialog(true)//是否显示为对话框样式
                .build();

        //用于解决TimePickerBuilder不想被覆盖又不想变成对话框的代码.前提条件是设置isDialog(true).
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        params.leftMargin = 0;
        params.rightMargin = 0;
        ViewGroup contentContainer = pvTime.getDialogContainerLayout();
        contentContainer.setLayoutParams(params);
        pvTime.getDialog().getWindow().setGravity(Gravity.BOTTOM);//可以改成Bottom

        pvTime.getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        pvTime.show();


    }
    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        return format.format(date);
    }

    public void setAddressInformation(String stringAddressInformationFromOther) {
        textViewAddressInformation.setText(stringAddressInformationFromOther+"\n");
    }

}
