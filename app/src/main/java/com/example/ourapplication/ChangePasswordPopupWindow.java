package com.example.ourapplication;

import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER_PASSWORD;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DIVER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_URL_DATABASE;
import static com.example.ourapplication.HandleMessageWhat.INT_ALLOW_CHANGE_PASSWORD;
import static com.example.ourapplication.HandleMessageWhat.INT_CAR_NUMBER_INPUT_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_CHANGE_FAILED;
import static com.example.ourapplication.HandleMessageWhat.INT_CHANGE_PASSWORD_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_DATABASE_CONNECTION_FAILURE;
import static com.example.ourapplication.HandleMessageWhat.INT_NOT_ALLOW_CHANGE_PASSWORD;
import static com.example.ourapplication.HandleMessageWhat.STRING_PASSWORD;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChangePasswordPopupWindow extends PopupWindow {
    private static final String TAG = "ChangePasswordPopupWindow";
    private Context mContext;
    private View mView;
    private TextInputEditText tietOldPassword=null;
    private TextInputEditText tietNewPassword_1=null;
    private TextInputEditText tietNewPassword_2=null;
    private Button buttonCancleChange=null;
    private Button buttonConfirmChange=null;
    private ProgressDialog progressDialog;
    private String UserName_HavedLoggedIn=null;
    private android.os.Handler handler=new Handler(Looper.getMainLooper()){
        @SuppressLint("LongLogTag")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //??????????????????????????????
                case INT_CHANGE_FAILED:
                    progressDialog.dismiss();
                    Toast.makeText(mContext,"????????????,??????????????????!XX0000",Toast.LENGTH_SHORT).show();
                    break;

                //??????????????????
                case INT_ALLOW_CHANGE_PASSWORD:
                    AllowToChangePassword();
                    break;
                    //?????????????????????
                case INT_NOT_ALLOW_CHANGE_PASSWORD:
                    progressDialog.dismiss();
                    Toast.makeText(mContext,"????????????????????????!",Toast.LENGTH_SHORT).show();
                    break;

                    //??????????????????
                case INT_CHANGE_PASSWORD_SUCCESS:
                    progressDialog.dismiss();
                    Toast.makeText(mContext,"??????????????????!",Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                    //?????????????????????
                case INT_DATABASE_CONNECTION_FAILURE:
                    progressDialog.dismiss();
                    Toast.makeText(mContext,"?????????????????????!",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public ChangePasswordPopupWindow(Activity context,String un){
        this.mContext=context;
        this.mView= LayoutInflater.from(mContext).inflate(R.layout.change_password_interface_layout,null);
        this.UserName_HavedLoggedIn=un;
        init();

    }
    private void init(){
        tietOldPassword = mView.findViewById(R.id.InputEidtText_old_Password);
        tietNewPassword_1 = mView.findViewById(R.id.InputEidtText_New_Password_1);
        tietNewPassword_2 = mView.findViewById(R.id.InputEidtText_New_Password_2);
        buttonCancleChange = mView.findViewById(R.id.button_Cancel_change_password_interface);
        buttonConfirmChange = mView.findViewById(R.id.button_confirm_change_password_interface);

        // ?????????????????????
        this.setOutsideTouchable(true);
        // mMenuView??????OnTouchListener????????????????????????????????????????????????????????????????????????
        this.mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mView.findViewById(R.id.constraintLayout_change_password_interface_max).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        /* ???????????????????????? */
        // ????????????
        this.setContentView(this.mView);
        // ??????????????????????????????
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        // ???????????????????????????
        this.setFocusable(true);
        // ???????????????ColorDrawable??????????????????
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // ???????????????????????????
        this.setBackgroundDrawable(dw);

        initProgressDialog();
        initOnClick();
    }

    private void initOnClick(){
        //???????????????????????????
        this.buttonCancleChange.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                Log.e(TAG,"??????..");
                dismiss();
            }
        });

        this.buttonConfirmChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void run() {


                            try {
                                Class.forName(STRING_DIVER);
                                Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//????????????
                                //????????????????????????
                                String sql ="select * from UsernameAndPassword_table where username = '"+UserName_HavedLoggedIn+"'";
                                PreparedStatement statement =  connection.prepareStatement(sql);
                                ResultSet rs = statement.executeQuery();
                                rs.next();
                                Log.e(TAG,"???????????????");
                                if(rs.getString(STRING_PASSWORD).equals(tietOldPassword.getText().toString())){
                                    handler.obtainMessage(INT_ALLOW_CHANGE_PASSWORD).sendToTarget();
                                }else{
                                    handler.obtainMessage(INT_NOT_ALLOW_CHANGE_PASSWORD).sendToTarget();
                                }
                                connection.close();
                                statement.close();
                            } catch (SQLException | ClassNotFoundException throwables) {

                                throwables.printStackTrace();
                            }

                        }
                    }).start();

            }
        });

    }
    //???????????????
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setIndeterminate(false);//????????????
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("??????????????????,?????????...");
        progressDialog.setCancelable(false);//false?????????????????????true??????????????????
    }

    private void AllowToChangePassword(){
        if(tietNewPassword_1.getText().toString().equals(tietNewPassword_2.getText().toString())){
            new Thread(new Runnable() {
                @SuppressLint("LongLogTag")
                @Override
                public void run() {
                    try {
                        Class.forName(STRING_DIVER);
                        Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//????????????
                        //????????????????????????
                        String sql ="update UsernameAndPassword_table set password = '"+tietNewPassword_1.getText().toString()+"' where username = '"+UserName_HavedLoggedIn+"'";
                        PreparedStatement statement =  connection.prepareStatement(sql);
                        int changeInger= statement.executeUpdate(sql);

                        if(changeInger<1){
                            handler.obtainMessage(INT_CHANGE_FAILED).sendToTarget();
                            return ;
                        }
                        Log.e(TAG,"??????????????????\t"+String.valueOf(changeInger));
                        connection.close();
                        statement.close();
                    } catch (SQLException | ClassNotFoundException throwables) {
                        throwables.printStackTrace();
                    }
                    handler.obtainMessage(INT_CHANGE_PASSWORD_SUCCESS).sendToTarget();

                }
            }).start();

        }
        else {
            progressDialog.dismiss();
            Toast.makeText(mContext,"????????????????????????!",Toast.LENGTH_SHORT).show();
        }

    }

}
