package com.example.ourapplication;



import static com.example.ourapplication.HandleMessageWhat.*;

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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PersonalFragment extends Fragment {
    private static final String TAG = "PersonalFragment";
    private View rootView=null;
    private ListView lvCarNumbers=null;
    private CarNumberStringAdapter adapter;
    private String UserName_HavedLoggedIn="test";
    private final String diver = "com.mysql.jdbc.Driver";
    private final String url = "jdbc:mysql://116.63.172.25:3306/SharedParking_db";
    private final String user = "user_sharedparking";//用户名
    private final String password = "123456";//密码
    private ConstraintLayout clAddCarNumber=null;
//    private final int MaxCarNumbers=5;
    //用于数据库传递信息
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG,"父线程接收到信息");
            switch (msg.what){
                case INT_READ_CARNUMBER_DATABASE_OK:
                    ReceiveAndCompleteCarNumbers((ArrayList<String>) msg.obj);
                    break;
            }
        }
    };





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.personal_fragment_layout, container, false);
        init();
        return rootView;
    }
    private void init(){
        lvCarNumbers=rootView.findViewById(R.id.lv_CarNumberList);
        clAddCarNumber=rootView.findViewById(R.id.constraintLayout_AddCarNmuber);
        adapter = new CarNumberStringAdapter(getContext(),R.layout.item_car_number_detailed_layout);
        lvCarNumbers.setAdapter(adapter);
        Log.e(TAG,"适配器设置成功");
        PutCarNumbersIntoListView();
        adapter.notifyDataSetChanged();
        Log.e(TAG,String.valueOf(adapter.getCount()));
    }


    private void PutCarNumbersIntoListView(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> CarNumbersAL=new ArrayList<String>();
                try {
                    Connection connection = DriverManager.getConnection(url, user, password);//获取连接
                    String sql = "select * from UserHavedCarNumbers_table where username ='"+UserName_HavedLoggedIn+"' ";
                    PreparedStatement statement =  connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    rs.next();
                    for(int i=2;i<=6;i++){
                        //表格中第一列是username 故从第二列开始是车牌号
                        String j=rs.getString(i);
                        if(!j.equals("null")){
                            CarNumbersAL.add(j);
                        }

                    }

                } catch (SQLException throwables) {
                    Log.e(TAG,"数据库相关代码抛出异常1");
                    throwables.printStackTrace();
                }
                Message msg= new Message();
                msg.what=INT_READ_CARNUMBER_DATABASE_OK;
                msg.obj=CarNumbersAL;
                handler.sendMessage(msg);

            }
        }).start();


    }

    //将读取到的车牌号写入适配器和ListView;
    private void ReceiveAndCompleteCarNumbers(ArrayList<String> al){
//        把原本已经写入的车牌全部删掉再重新添加
        adapter.clear();
        for(int i =0;i<al.size();i++)
            adapter.add(al.get(i));
        adapter.notifyDataSetChanged();
        //如果车牌数量大于等于5个 ,那就不能再添加车牌了
        if(adapter.getCount()>=5)clAddCarNumber.setVisibility(View.INVISIBLE);

    }

    //设置点击监听器
    private void initSetOnClick(){
        clAddCarNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                    }
                }).start();

            }
        });
    }
}
