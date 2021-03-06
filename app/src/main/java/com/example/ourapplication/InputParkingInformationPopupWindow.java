package com.example.ourapplication;

import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER_PASSWORD;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DIVER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_URL_DATABASE;
import static com.example.ourapplication.HandleMessageWhat.INT_CAR_NUMBER_INPUT_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_CHANGE_FAILED;
import static com.example.ourapplication.HandleMessageWhat.INT_CONNECT_FAILED;
import static com.example.ourapplication.HandleMessageWhat.INT_CREAT_ORDER_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_DeleteCarNumber_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_GET_INFORMATION_OF_ORDER;
import static com.example.ourapplication.HandleMessageWhat.INT_PARKING_TIME_OUT;
import static com.example.ourapplication.HandleMessageWhat.INT_READ_CARNUMBER_DATABASE_OK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.Marker;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InputParkingInformationPopupWindow extends PopupWindow {
    private static final String TAG = "PopupWindow";
    private Context mContext;
    private View mView;
    private Button bottonCancle=null;//??????
    private Button bottonSubmit=null;//??????
    private TextView textViewParkingEndTime=null;
    private TextView textViewAddressInformation=null;
    private TextView textViewParkingStartTime=null;
    private TimePickerView pvTime;
    private AutoCompleteTextView ACTV_Choose_Carnumbers=null;//??????????????????
    private String UserName_HavedLoggedIn=null;
    private ArrayAdapter<String> adapter=null;
    private String markerID=null;
    private Date ChoosedDate=null;
    private ProgressDialog progressDialog;
    private Marker marker;
    private Handler Phandler;//????????????Handler
    private String OrderIDForInit2;
    private String DetailAddress;

    //???????????????????????????
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case INT_CREAT_ORDER_SUCCESS:
                    Toast.makeText(mContext, "??????????????????!",Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;

                //?????????????????????????????????
                case INT_READ_CARNUMBER_DATABASE_OK:
                    initAdapter((ArrayList<String>) msg.obj);
                    Log.e(TAG,"????????????????????????");
                    break;

                case  INT_CONNECT_FAILED:
                    Toast.makeText(mContext, "?????????????????????,????????????????????????!XE##330",Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                case INT_GET_INFORMATION_OF_ORDER:
                    Bundle sss=(Bundle) msg.obj;
                    textViewParkingStartTime=mView.findViewById(R.id.textView512312);
                    textViewParkingStartTime.setText(sss.getString("endtime"));
                    textViewParkingEndTime.setText(sss.getString("endtime"));
                    break;
            }
        }
    };


    public InputParkingInformationPopupWindow(Activity context, Bundle information, Marker marker,Handler Phander){
        this.mContext=context;
        this.marker=marker;
        this.Phandler=Phander;
        this.mView= LayoutInflater.from(mContext).inflate(R.layout.layout_complete_parking_information,null);
        this.UserName_HavedLoggedIn=information.getString("username");
        this.markerID=information.getString("markerID");
        this.OrderIDForInit2=information.getString("OrderID");
        this.DetailAddress=information.getString("detailedAddress");
        if(information.getBoolean("OccupiedBoolean"))
            init2();

        else{
            init();
            getCarnumbers();
        }




    }
    private void init(){
        initProgressDialog();
        this.ACTV_Choose_Carnumbers=mView.findViewById(R.id.textInputLayout_Choose_CarNumbers);

        // ?????????????????????
        this.setOutsideTouchable(true);
        // mMenuView??????OnTouchListener????????????????????????????????????????????????????????????????????????
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
        //marker???????????????
        textViewAddressInformation=mView.findViewById(R.id.textView_address);

//        ?????????????????????
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
                DetectVariousInformation();
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

    //??????????????????????????????
    private void init2(){
        // ?????????????????????
        this.setOutsideTouchable(true);
        // mMenuView??????OnTouchListener????????????????????????????????????????????????????????????????????????
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
        //marker???????????????
        textViewParkingEndTime=mView.findViewById(R.id.textView_parking_end_time);
        textViewAddressInformation=mView.findViewById(R.id.textView_address);
        bottonCancle=mView.findViewById(R.id.buttonCompleteParkingInformation_Cancel);
        bottonSubmit=mView.findViewById(R.id.buttonCompleteParkingInformation_submit);
        bottonSubmit.setEnabled(false);
        bottonCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        this.ACTV_Choose_Carnumbers=mView.findViewById(R.id.textInputLayout_Choose_CarNumbers);
        ACTV_Choose_Carnumbers.setEnabled(false);
        initProgressDialog();
        Init2_getInf();
    }

    private void Init2_getInf(){
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//????????????
                    String sql = "select * from Order_Username_table where OrderID ='"+OrderIDForInit2+"' ";
                    PreparedStatement statement =  connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    rs.next();
                    Timestamp endTime=rs.getTimestamp("endtime");
                    Timestamp NowtTime= new Timestamp(new Date().getTime());
                    Bundle sss=new Bundle();
                    sss.putString("endtime",endTime.toString());
                    sss.putString("starttime",rs.getTimestamp("starttime").toString());
                    handler.obtainMessage(INT_GET_INFORMATION_OF_ORDER,sss).sendToTarget();
                    statement.close();
                    rs.close();
                    if(NowtTime.getTime()>=endTime.getTime()){
                        String sqlChangeMerkerInf="update SharedParkingMarkerCoordinate_table set OccupiedBoolean=false where markerID='"+markerID+"' ";
                        PreparedStatement statement3 =  connection.prepareStatement(sqlChangeMerkerInf);
                        int updatenumber2=statement3.executeUpdate(sqlChangeMerkerInf);
                        Phandler.obtainMessage(INT_PARKING_TIME_OUT,marker).sendToTarget();
                    }


                    connection.close();
                    progressDialog.dismiss();
                } catch (ClassNotFoundException | SQLException throwables) {
                    handler.obtainMessage(INT_CONNECT_FAILED,null).sendToTarget();
                    progressDialog.dismiss();
                    throwables.printStackTrace();
                }
            }
        }).start();
    }


    private void CreateSelectTime(){
        //???????????????
        Calendar selectedDate = Calendar.getInstance();//??????????????????
        Calendar endDate = Calendar.getInstance();//???????????????
        endDate.set(2023, 11, 31);
        pvTime = new TimePickerBuilder(getContentView().getContext(), new OnTimeSelectListener() {
            //??????????????????
            @Override
            public void onTimeSelect(Date date, View v) {

//                Toast.makeText(getContentView().getContext(), getTime(date), Toast.LENGTH_SHORT).show();
                ChoosedDate=date;
//                Log.e(TAG,date.toString());
                textViewParkingEndTime.setText(getTime(date));
            }
        }).setType(new boolean[]{true, true, true, true, false, false})
                // ??????????????????
                .setCancelText("??????")//??????????????????
                .setSubmitText("??????")//??????????????????
                .setTitleSize(20)//??????????????????
                .setTitleText("????????????")//????????????
                .setOutSideCancelable(true)//???????????????????????????????????????????????????????????????
                .isCyclic(false)//??????????????????
                .setRangDate(selectedDate,endDate)//???????????????????????????
                .setLabel("???","???","???","???","???","???")//?????????????????????????????????
//                ???TimePickerBuilder?????????????????????,??????????????????PopupWindow??????
                .isDialog(true)//??????????????????????????????
                .build();

        //????????????TimePickerBuilder????????????????????????????????????????????????.?????????????????????isDialog(true).
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        params.leftMargin = 0;
        params.rightMargin = 0;
        ViewGroup contentContainer = pvTime.getDialogContainerLayout();
        contentContainer.setLayoutParams(params);
        pvTime.getDialog().getWindow().setGravity(Gravity.BOTTOM);//????????????Bottom

        pvTime.getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        pvTime.show();


    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy???MM???dd??? HH:mm");

        return format.format(date);
    }


    public void setAddressInformation(String stringAddressInformationFromOther) {
        textViewAddressInformation.setText(stringAddressInformationFromOther+"\n");
    }

    //??????????????????
    private void initAdapter(ArrayList<String> listCarnumber){
        this.adapter=new ArrayAdapter<String>(mContext,R.layout.item_car_number_when_input,listCarnumber);
        this.ACTV_Choose_Carnumbers.setAdapter(this.adapter);

    }


    //??????????????????
    private void getCarnumbers(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> CarNumbersAL=new ArrayList<String>();
                try {

                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//????????????
                    String sql = "select * from UserHavedCarNumbers_table where username ='"+UserName_HavedLoggedIn+"' ";
                    PreparedStatement statement =  connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    rs.next();
                    for(int i=2;i<=6;i++){//?????????????????????username ?????????????????????????????????
                        String ss=new String(rs.getString(i));
                        if(!ss.equals("null")){
                            CarNumbersAL.add(ss);
                        }
                    }
//???????????????????????????????????????,??????????????????????????????
                } catch (ClassNotFoundException | SQLException throwables) {
                    Log.e(TAG,"?????????????????????????????????1");
                    handler.obtainMessage(INT_CONNECT_FAILED,null).sendToTarget();
                    throwables.printStackTrace();
                }
                Message msg= new Message();
                msg.what=INT_READ_CARNUMBER_DATABASE_OK;
                msg.obj=CarNumbersAL;
                handler.sendMessage(msg);
            }
        }).start();

    }

    //????????????????????????????????????
    void DetectVariousInformation(){

        //????????????????????????????????????????????????????????????
        if(!this.ACTV_Choose_Carnumbers.getText().toString().equals("")&&this.ChoosedDate!=null&&(new Date()).getTime()<ChoosedDate.getTime()){
            progressDialog.show();
            CreateOrder();
        }
        else {
            new MaterialAlertDialogBuilder(mContext).setTitle("").
                    setMessage(mView.getResources().getText(R.string.String_Incomplete_Information)).
                    setNegativeButton(mView.getResources().getText(R.string.String_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }


    }

    //????????????  ???????????????SQL??????????????????
    private void  CreateOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//????????????
//?????????????????????id
                    String sqlInsertOrderID = "insert into Order_Username_table(OrderID) select substring(md5(rand()), 1, 20)";
                    PreparedStatement statement =  connection.prepareStatement(sqlInsertOrderID);
                    int InsertNumber= statement.executeUpdate(sqlInsertOrderID);
                    Log.e(TAG,"??????OrderID");
                    //??????????????????
                    statement.close();

                    if(InsertNumber>=1) {
                        //???????????????OrderID
                        String sqlGetOrderID="select OrderID from Order_Username_table where markerID='null'";
                        PreparedStatement statement4 =  connection.prepareStatement(sqlGetOrderID);
                        ResultSet rsOrderID =statement4.executeQuery(sqlGetOrderID);
                        Log.e(TAG,sqlGetOrderID);
                        rsOrderID.next();
                        String OrderID= rsOrderID.getString(1);
                        Log.e(TAG,OrderID);
                        statement4.close();



                        //??????????????????
                        Timestamp startTime= new Timestamp((new Date()).getTime());
                        //??????????????????
                        Timestamp endTime=new Timestamp(ChoosedDate.getTime());
//???????????????????????????,???????????????SQL????????????????????????,?????? ?????????????????????........... 10???22??? 18?????????
                        String sqlUpdateOtherInf ="update Order_Username_table set markerID='"+markerID
                                +"',username='"+UserName_HavedLoggedIn
                                +"',carnumber='"+ACTV_Choose_Carnumbers.getText().toString()
                                +"',starttime='"+startTime+"' ,endtime='"+endTime+"',detailedAddress='"+DetailAddress+"'  where markerID='null'";
                        PreparedStatement statement2 =  connection.prepareStatement(sqlUpdateOtherInf);
                        int updatenumber=statement2.executeUpdate(sqlUpdateOtherInf);
                        statement2.close();


                        if(updatenumber>=1){
                            Log.e(TAG,"0.0");
                            String sqlChangeMerkerInf="update SharedParkingMarkerCoordinate_table set OccupiedBoolean=true,OrderID='"+OrderID+"' where markerID='"+markerID+"' ";
                            Log.e(TAG,sqlChangeMerkerInf);
                            PreparedStatement statement3 =  connection.prepareStatement(sqlChangeMerkerInf);
                            int updatenumber2=statement3.executeUpdate(sqlChangeMerkerInf);
                            Log.e(TAG,"??????Marker?????????!");
                            if(updatenumber2>=1){
                                handler.obtainMessage(INT_CREAT_ORDER_SUCCESS).sendToTarget();
                                Phandler.obtainMessage(INT_CREAT_ORDER_SUCCESS,marker).sendToTarget();
                            }
                        }

                    }
                    connection.close();
                    progressDialog.dismiss();

                } catch (ClassNotFoundException | SQLException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }

            }
        }).start();

    }

    //???????????????
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setIndeterminate(false);//????????????
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("???????????????????????????...");
        progressDialog.setCancelable(false);//false?????????????????????true??????????????????
    }

//    private void CreateOrder2(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Class.forName(STRING_DIVER);
//                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//????????????
//                    Log.e(TAG,"2????????????");
//                    //????????????????????????
//                        Timestamp startTime= new Timestamp((new Date()).getTime());
//                        Log.e(TAG,String.valueOf((new Date()).getTime()));
//                        //??????????????????
//                        Timestamp endTime=new Timestamp(ChoosedDate.getTime());
//                        //????????????????????????
//
//
////???????????????????????????,???????????????SQL????????????????????????,?????? ?????????????????????........... 10???22??? 18?????????
//
//                    String sql2 ="update Order_Username_table set markerID='"+markerID
//                                +"',username='"+UserName_HavedLoggedIn
//                                +"',carnumber='"+ACTV_Choose_Carnumbers.getText().toString()
//                                +"',starttime='"+startTime+"' ,endtime='"+endTime+"'  where markerID='null'";
////                    Log.e(TAG,sql2);
//                        PreparedStatement statement2 =  connection.prepareStatement(sql2);
//                    Log.e(TAG,statement2.toString());
//                    int updatenumber=statement2.executeUpdate(sql2);
//
//                        Log.e(TAG,"????????????????????????!");
////                        if(updatenumber==1){
////                            Log.e(TAG,"??????3");
////                            connection.close();
////                            statement2.close();
////                           CreateOrder3();
////                        }
//                    connection.close();
//                    statement2.close();
//                    handler.obtainMessage(2).sendToTarget();
//                } catch (ClassNotFoundException | SQLException e) {
//                    Log.e(TAG,"??????2??????");
//                    progressDialog.dismiss();
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//    private void CreateOrder3(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Class.forName(STRING_DIVER);
//                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//????????????
//                        //??????SharedParkingMarkerCoordinate_table????????????,???????????????????????????????????????????????????
//                        String sql3="update SharedParkingMarkerCoordinate_table set OccupiedBoolean= true where markerID='"+markerID+"'";
//                        PreparedStatement statement3 =  connection.prepareStatement(sql3);
//                        int updatenumber2=statement3.executeUpdate(sql3);
//                        Log.e(TAG,"??????Marker?????????!");
//                        if(updatenumber2==1){
//                            handler.obtainMessage(INT_CREAT_ORDER_SUCCESS).sendToTarget();
//                            Phandler.obtainMessage(INT_CREAT_ORDER_SUCCESS,marker).sendToTarget();
//                        }
//                    connection.close();
//                    statement3.close();
//                } catch (ClassNotFoundException | SQLException e) {
//                    Log.e(TAG,"??????3??????");
//                    progressDialog.dismiss();
//                    e.printStackTrace();
//                }
//                progressDialog.dismiss();
//            }
//        }).start();
//    }

}
