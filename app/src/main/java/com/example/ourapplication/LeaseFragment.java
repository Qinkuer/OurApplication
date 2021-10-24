package com.example.ourapplication;


import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER_PASSWORD;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DIVER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_URL_DATABASE;
import static com.example.ourapplication.HandleMessageWhat.INT_DATABASE_CONNECTION_FAILURE;
import static com.example.ourapplication.HandleMessageWhat.INT_GET_ALL_ORDER_SUCCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_GET_USER_ALL_PARKING_SUCCCESS;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class LeaseFragment extends Fragment {
    private static final String TAG = "LeasseFragment";
    private String UserName_HavedLoggedIn=null;
    private ProgressDialog progressDialog=null;
    private LeaseParkingsAdapter adapter=null;
    private ListView lvParking=null;
    private View  rootView;
    private FloatingActionButton FAB_add_Parking=null;
    private android.os.Handler handler=new Handler(Looper.getMainLooper()){
        @SuppressLint("LongLogTag")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case INT_GET_USER_ALL_PARKING_SUCCCESS:
                    WhenGetOrderItems((ArrayList<LeaseParkingInf_Item_ForAdapter>) msg.obj);
                    break;
                //数据库连接失败
                case INT_DATABASE_CONNECTION_FAILURE:
                    Toast.makeText(getContext(),"数据库连接出了问题!",Toast.LENGTH_SHORT).show();
                    break;
            }
            progressDialog.dismiss();
        }
    };



    public LeaseFragment(String UserName){
        super();
        this.UserName_HavedLoggedIn=UserName;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.lease_fragment_layout, container, false);
        initProgressDialog();
        progressDialog.show();
        init();
        return rootView;
    }

    private void init(){
        this.lvParking=rootView.findViewById(R.id.listView_LeaseAllParking);
        this.adapter=new LeaseParkingsAdapter(getContext(),R.layout.item_lease_parking);
        this.FAB_add_Parking=rootView.findViewById(R.id.floating_action_button_Add_LeaseParkingMarker);
        this.lvParking.setAdapter(this.adapter);
        initOnClick();


        getLeaseParkingItems();
    }

    private void initOnClick() {
        this.FAB_add_Parking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
    }

    private void getLeaseParkingItems(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
                    String sql = "select B.detailedAddress,A.starttime,A.endtime  from Order_Username_table as A,SharedParkingMarkerCoordinate_table as B where A.OrderID=B.OrderID AND B.Ownerusername='"+
                            UserName_HavedLoggedIn+"' ";

                    PreparedStatement statement =  connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    Timestamp NowTime=new Timestamp((new Date().getTime()));

                    ArrayList<LeaseParkingInf_Item_ForAdapter> AL_Result=new ArrayList<LeaseParkingInf_Item_ForAdapter>();
                    while (rs.next()){
                        AL_Result.add(new LeaseParkingInf_Item_ForAdapter(rs.getString("detailedAddress"),
                                    rs.getTimestamp("starttime"),
                                    rs.getTimestamp("endtime")
                            ));
                    }
                    connection.close();
                    statement.close();

                    handler.obtainMessage(INT_GET_USER_ALL_PARKING_SUCCCESS,AL_Result).sendToTarget();
                } catch (SQLException | ClassNotFoundException throwables) {
                    Log.e(TAG,"数据库相关代码抛出异常1123");
                    handler.obtainMessage(INT_DATABASE_CONNECTION_FAILURE).sendToTarget();
                    throwables.printStackTrace();
                }
            }
        }).start();

    }

    private void WhenGetOrderItems(ArrayList<LeaseParkingInf_Item_ForAdapter> AL_lpiifa){
        adapter.clear();
        adapter.addAll(AL_lpiifa);
        adapter.notifyDataSetChanged();
    }



    //初始化弹窗
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在加载请稍等...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
    }



}
class LeaseParkingInf_Item_ForAdapter{
    String detailedAddress;
    Timestamp startTime;
    Timestamp endTime;
    public LeaseParkingInf_Item_ForAdapter(String da,Timestamp sT,Timestamp eT){
        this.startTime=sT;
        this.endTime=eT;
        this.detailedAddress=da;

    }
}