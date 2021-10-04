package com.example.ourapplication;

import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER_PASSWORD;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DIVER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_URL_DATABASE;
import static com.example.ourapplication.HandleMessageWhat.INT_CAR_NUMBER_INPUT_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_READ_CARNUMBER_DATABASE_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class InputNewCarNumberPopupWindow extends PopupWindow {
    private static final String TAG = "InputNCNPopupWindow";
    private Context mContext;
    private View mView;
    private Button buttonCancle=null;
    private Button buttonConfirm=null;
    private TextInputEditText tietNewCarNumber=null;
    private ProgressDialog progressDialog;
    private String UserName_HavedLoggedIn=null;
    private ArrayList<Integer> db_CarNumber_is_null=null;//记录车牌数据库第几列是null
    public final String CarNumberPattern = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$";
    private Handler partentHandler=null;

    private android.os.Handler handler=new Handler(Looper.getMainLooper()){
        @SuppressLint("LongLogTag")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG,"父线程接收到信息");
            switch (msg.what){
                case INT_CAR_NUMBER_INPUT_SUCCESS:
                    AddCarNumberSuccess();
                    break;
            }
        }
    };

    public InputNewCarNumberPopupWindow(Activity context, String Username, ArrayList<Integer> AL_is_null, Handler phander){
        this.mContext=context;
        this.mView= LayoutInflater.from(mContext).inflate(R.layout.input_new_car_number_layout,null);
        this.UserName_HavedLoggedIn=Username;
        this.db_CarNumber_is_null=AL_is_null;
        this.partentHandler=phander;
        init();

    }
    private void init(){
        this.buttonCancle=mView.findViewById(R.id.button_Cancel_InputNewCarNumber);
        this.buttonConfirm=mView.findViewById(R.id.button_confirm_InputNewCarNumber);
        this.tietNewCarNumber=mView.findViewById(R.id.InputEidtText_NewCarNumber);

        // 设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        this.mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mView.findViewById(R.id.constraintLayout_input_new_car_number).getTop();
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

        initOnClick();
    }

    private void initOnClick(){

        //关闭按钮的点击事件
        this.buttonCancle.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                Log.e(TAG,"取消..");
                dismiss();
            }
        });

        //确认按钮的点击事件
        this.buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                //先判断输入是否正确,再写入数据库.
                if(Pattern.matches(CarNumberPattern,tietNewCarNumber.getText())){
                    Log.e(TAG,"车牌符合规范");
                    //输入的车牌号规范
                    new Thread(new Runnable() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void run() {
                            //插入车牌号
                            try {
                                Class.forName(STRING_DIVER);
                                Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
                                //修改指定列的内容
                                String sql = "update UserHavedCarNumbers_table set carnumber"
                                        +String.valueOf(db_CarNumber_is_null.get(0))+" = '"
                                        +tietNewCarNumber.getText()+"' where username = '"
                                        + UserName_HavedLoggedIn+"' ";

                                PreparedStatement statement =  connection.prepareStatement(sql);
                                int changeInger= statement.executeUpdate(sql);
                                if(changeInger<1){
                                    Toast.makeText(mContext, "添加失败,请联系管理员!XE4135351",Toast.LENGTH_SHORT).show();
                                    return ;
                                }



                            } catch (SQLException | ClassNotFoundException throwables) {
                                Log.e(TAG,"数据库相关代码抛出异常1");
                                throwables.printStackTrace();
                            }
                            handler.obtainMessage(INT_CAR_NUMBER_INPUT_SUCCESS,null).sendToTarget();
                            Log.e(TAG,"插入成功!");
                        }
                    }).start();
                }
                else{
                    //输入的不规范
                    Toast.makeText(mContext,"输入的车牌号不正确!",Toast.LENGTH_SHORT).show();

                }


            }
        });
    }



    private void AddCarNumberSuccess(){
        Toast.makeText(mContext, "添加车牌: "+tietNewCarNumber.getText()+" 成功!",Toast.LENGTH_SHORT).show();
        Log.e(TAG,"关闭");
        partentHandler.obtainMessage(INT_CAR_NUMBER_INPUT_SUCCESS,null).sendToTarget();
        dismiss();

    }
}
