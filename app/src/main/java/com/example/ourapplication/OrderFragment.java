package com.example.ourapplication;


import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER_PASSWORD;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DIVER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_URL_DATABASE;
import static com.example.ourapplication.HandleMessageWhat.INT_CAR_NUMBER_INPUT_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_CHANGE_FAILED;
import static com.example.ourapplication.HandleMessageWhat.INT_CONNECT_FAILED;
import static com.example.ourapplication.HandleMessageWhat.INT_DATABASE_CONNECTION_FAILURE;
import static com.example.ourapplication.HandleMessageWhat.INT_GET_ALL_ORDER_SUCCCESS;

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
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class OrderFragment extends Fragment {
    private static final String TAG = "OrderFragment";
    private View  rootView;
    private String UserName_HavedLoggedIn=null;
    private OrderListAdapter adapter=null;
    private ListView lvOrderItems=null;
    private ProgressDialog progressDialog=null;

    private android.os.Handler handler=new Handler(Looper.getMainLooper()){
        @SuppressLint("LongLogTag")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case INT_GET_ALL_ORDER_SUCCCESS:
                    WhenGetOrderItems((ArrayList<OrderInf_Item_ForAdapter>) msg.obj);
                    break;
                //数据库连接失败
                case INT_DATABASE_CONNECTION_FAILURE:
                    Toast.makeText(getContext(),"数据库连接出了问题!",Toast.LENGTH_SHORT).show();
                    break;
            }
            progressDialog.dismiss();
        }
    };

    public OrderFragment(String UserName){
        super();
        this.UserName_HavedLoggedIn=UserName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.order_fragment_layout, container, false);
        initProgressDialog();
        progressDialog.show();
        init();
        return rootView;
    }

    private void init() {
        this.lvOrderItems=rootView.findViewById(R.id.listView_OrderItems);
        this.adapter=new OrderListAdapter(getContext(),R.layout.item_order);
        this.lvOrderItems.setAdapter(this.adapter);
        getOrderItems();
    }


    private void getOrderItems(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
                    String sql = "select * from Order_Username_table where username ='"+UserName_HavedLoggedIn+"' ";

                    PreparedStatement statement =  connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    Timestamp NowTime=new Timestamp((new Date().getTime()));

                    ArrayList<OrderInf_Item_ForAdapter> AL_Result=new ArrayList<OrderInf_Item_ForAdapter>();
                    ArrayList<OrderInf_Item_ForAdapter> AL_New=new ArrayList<OrderInf_Item_ForAdapter>();
                    ArrayList<OrderInf_Item_ForAdapter> AL_Old=new ArrayList<OrderInf_Item_ForAdapter>();
                    while (rs.next()){
                        //逃避排序
                        //获取订单的信息.
                        if(NowTime.getTime()-rs.getTimestamp("endtime").getTime()>-100){
                            AL_Old.add(new OrderInf_Item_ForAdapter(rs.getString("detailedAddress"),
                                    rs.getTimestamp("starttime"),
                                    rs.getTimestamp("endtime")
                            ));
                        }
                        else {
                            AL_New.add(new OrderInf_Item_ForAdapter(rs.getString("detailedAddress"),
                                    rs.getTimestamp("starttime"),
                                    rs.getTimestamp("endtime")
                            ));
                        }

                    }

                    AL_Result.addAll(AL_New);
                    AL_Result.addAll(AL_Old);

                    handler.obtainMessage(INT_GET_ALL_ORDER_SUCCCESS,AL_Result).sendToTarget();
                    connection.close();
                    statement.close();
                } catch (SQLException | ClassNotFoundException throwables) {
                    Log.e(TAG,"数据库相关代码抛出异常1123");
                    handler.obtainMessage(INT_DATABASE_CONNECTION_FAILURE).sendToTarget();
                    throwables.printStackTrace();
                }
            }
        }).start();

    }

    private void WhenGetOrderItems(ArrayList<OrderInf_Item_ForAdapter> OIIFA){
            adapter.clear();
            adapter.addAll(OIIFA);
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
class OrderInf_Item_ForAdapter{
    String detailedAddress;
    Timestamp startTime;
    Timestamp endTime;
    public OrderInf_Item_ForAdapter(String da,Timestamp sT,Timestamp eT){
        this.startTime=sT;
        this.endTime=eT;
        this.detailedAddress=da;

    }
}
