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
    private Button bottonCancle=null;//取消
    private Button bottonSubmit=null;//确定
    private TextView textViewParkingEndTime=null;
    private TextView textViewAddressInformation=null;
    private TextView textViewParkingStartTime=null;
    private TimePickerView pvTime;
    private AutoCompleteTextView ACTV_Choose_Carnumbers=null;//车牌号下拉栏
    private String UserName_HavedLoggedIn=null;
    private ArrayAdapter<String> adapter=null;
    private String markerID=null;
    private Date ChoosedDate=null;
    private ProgressDialog progressDialog;
    private Marker marker;
    private Handler Phandler;//主进程的Handler
    private String OrderIDForInit2;
    private String DetailAddress;

    //用于数据库传递信息
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case INT_CREAT_ORDER_SUCCESS:
                    Toast.makeText(mContext, "创建订单成功!",Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;

                //读取用户拥有的车牌成功
                case INT_READ_CARNUMBER_DATABASE_OK:
                    initAdapter((ArrayList<String>) msg.obj);
                    Log.e(TAG,"获取了所有车牌号");
                    break;

                case  INT_CONNECT_FAILED:
                    Toast.makeText(mContext, "服务器连接失败,或者该坐标有问题!XE##330",Toast.LENGTH_SHORT).show();
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

    //当该车位被占用时启用
    private void init2(){
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
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
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
        //时间选择器
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar endDate = Calendar.getInstance();//最长的时间
        endDate.set(2023, 11, 31);
        pvTime = new TimePickerBuilder(getContentView().getContext(), new OnTimeSelectListener() {
            //确认选择日期
            @Override
            public void onTimeSelect(Date date, View v) {

//                Toast.makeText(getContentView().getContext(), getTime(date), Toast.LENGTH_SHORT).show();
                ChoosedDate=date;
//                Log.e(TAG,date.toString());
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

    //初始化适配器
    private void initAdapter(ArrayList<String> listCarnumber){
        this.adapter=new ArrayAdapter<String>(mContext,R.layout.item_car_number_when_input,listCarnumber);
        this.ACTV_Choose_Carnumbers.setAdapter(this.adapter);

    }


    //获取车牌列表
    private void getCarnumbers(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> CarNumbersAL=new ArrayList<String>();
                try {

                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
                    String sql = "select * from UserHavedCarNumbers_table where username ='"+UserName_HavedLoggedIn+"' ";
                    PreparedStatement statement =  connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    rs.next();
                    for(int i=2;i<=6;i++){//表格中第一列是username 故从第二列开始是车牌号
                        String ss=new String(rs.getString(i));
                        if(!ss.equals("null")){
                            CarNumbersAL.add(ss);
                        }
                    }
//不知道为什么连接失败会异常,但是不运行下面的代码
                } catch (ClassNotFoundException | SQLException throwables) {
                    Log.e(TAG,"数据库相关代码抛出异常1");
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

    //对输入的所有信息进行检测
    void DetectVariousInformation(){

        //所有信息都填写了并且时间没有选择错误才可
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

    //创建订单  不考虑任何SQL语句执行失败
    private void  CreateOrder(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
//创建对应的订单id
                    String sqlInsertOrderID = "insert into Order_Username_table(OrderID) select substring(md5(rand()), 1, 20)";
                    PreparedStatement statement =  connection.prepareStatement(sqlInsertOrderID);
                    int InsertNumber= statement.executeUpdate(sqlInsertOrderID);
                    Log.e(TAG,"生成OrderID");
                    //说明插入成功
                    statement.close();

                    if(InsertNumber>=1) {
                        //获取生成的OrderID
                        String sqlGetOrderID="select OrderID from Order_Username_table where markerID='null'";
                        PreparedStatement statement4 =  connection.prepareStatement(sqlGetOrderID);
                        ResultSet rsOrderID =statement4.executeQuery(sqlGetOrderID);
                        Log.e(TAG,sqlGetOrderID);
                        rsOrderID.next();
                        String OrderID= rsOrderID.getString(1);
                        Log.e(TAG,OrderID);
                        statement4.close();



                        //获取当前时间
                        Timestamp startTime= new Timestamp((new Date()).getTime());
                        //获取截止时间
                        Timestamp endTime=new Timestamp(ChoosedDate.getTime());
//原本不知道什么原因,下面的语句SQL是不能执行成功的,但是 试着试着就能了........... 10月22日 18时左右
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
                            Log.e(TAG,"更新Marker表成功!");
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

    //初始化弹窗
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在获取信息请稍等...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
    }

//    private void CreateOrder2(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Class.forName(STRING_DIVER);
//                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
//                    Log.e(TAG,"2连接成功");
//                    //获取系统当前时间
//                        Timestamp startTime= new Timestamp((new Date()).getTime());
//                        Log.e(TAG,String.valueOf((new Date()).getTime()));
//                        //获取截止时间
//                        Timestamp endTime=new Timestamp(ChoosedDate.getTime());
//                        //更新该条订单信息
//
//
////原本不知道什么原因,下面的语句SQL是不能执行成功的,但是 试着试着就能了........... 10月22日 18时作业
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
//                        Log.e(TAG,"更新订单信息成功!");
////                        if(updatenumber==1){
////                            Log.e(TAG,"执行3");
////                            connection.close();
////                            statement2.close();
////                           CreateOrder3();
////                        }
//                    connection.close();
//                    statement2.close();
//                    handler.obtainMessage(2).sendToTarget();
//                } catch (ClassNotFoundException | SQLException e) {
//                    Log.e(TAG,"执行2失败");
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
//                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
//                        //更新SharedParkingMarkerCoordinate_table表的信息,让图标改变颜色，使其其他人不能点击
//                        String sql3="update SharedParkingMarkerCoordinate_table set OccupiedBoolean= true where markerID='"+markerID+"'";
//                        PreparedStatement statement3 =  connection.prepareStatement(sql3);
//                        int updatenumber2=statement3.executeUpdate(sql3);
//                        Log.e(TAG,"更新Marker表成功!");
//                        if(updatenumber2==1){
//                            handler.obtainMessage(INT_CREAT_ORDER_SUCCESS).sendToTarget();
//                            Phandler.obtainMessage(INT_CREAT_ORDER_SUCCESS,marker).sendToTarget();
//                        }
//                    connection.close();
//                    statement3.close();
//                } catch (ClassNotFoundException | SQLException e) {
//                    Log.e(TAG,"执行3失败");
//                    progressDialog.dismiss();
//                    e.printStackTrace();
//                }
//                progressDialog.dismiss();
//            }
//        }).start();
//    }

}
