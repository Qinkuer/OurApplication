package com.example.ourapplication;



import static com.example.ourapplication.ConnectDataBaseClass.*;
import static com.example.ourapplication.HandleMessageWhat.*;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
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
    private ConstraintLayout clAddCarNumber=null;
    private ArrayList<Integer>  db_CarNumber_is_null=new ArrayList<Integer>();//记录车牌数据库第几列是null
//    private final int MaxCarNumbers=5;
    //用于数据库传递信息
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG,"父线程接收到信息");
            switch (msg.what){
                //读取用户拥有的车牌成功
                case INT_READ_CARNUMBER_DATABASE_OK:
                    ReceiveAndCompleteCarNumbers((ArrayList<String>) msg.obj);
                    break;
                    //给用户插入新车牌成功
                case INT_CAR_NUMBER_INPUT_SUCCESS:
                    PutCarNumbersIntoListView();
                    break;
                case  INT_CONNECT_FAILED:
                    Toast.makeText(requireActivity(), "服务器连接失败!XE##330",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    public PersonalFragment(String UserName){
        super();
        this.UserName_HavedLoggedIn=UserName;
    }




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
        initSetOnClick();
    }


    private void PutCarNumbersIntoListView(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> CarNumbersAL=new ArrayList<String>();
                db_CarNumber_is_null.clear();//将原本保存的数据库中哪一列是null的编号清除.
                try {
                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
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
                        else {
                            db_CarNumber_is_null.add(i-1);
                        }

                    }

                } catch (SQLException | ClassNotFoundException throwables) {
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
//点击弹出窗口,
                if(db_CarNumber_is_null.size()>=1){
                    InputNewCarNumberPopupWindow IPIPopupWindow = new InputNewCarNumberPopupWindow(requireActivity(),UserName_HavedLoggedIn,db_CarNumber_is_null,handler);

                    IPIPopupWindow.showAtLocation(rootView.findViewById(R.id.constraintLayout_Main_personal_fragment), Gravity.CENTER,0,0);

                }
                else{
                    Toast.makeText(getContext(), "最多持有5个车牌!或者服务器未连接上",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }


}
