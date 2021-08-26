package com.example.ourapplication;


import static com.example.ourapplication.HandleMessageWhat.*;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DB_DataProofreadingLooperThreadClass extends Thread {
    private final String diver = "com.mysql.jdbc.Driver";
    private static String TAG="DB_DataProofreadingLooperThread";
    private final String url = "jdbc:mysql://116.63.172.25:3306/SharedParking_db";
//    private static String url = "jdbc:mysql://116.63.172.25:3306/SharedParking_db?serverTimezone=GMT%2B8&useSSL=FALSE";
    private final String user = "user_sharedparking";//用户名
    private final String password = "123456";//密码

    private Connection connection=null;
    public static Handler handler=null;
    public static Handler Mainhandler=null;




    @Override
    public void run(){
        super.run();

        try {
            Class.forName(diver);
            connection = DriverManager.getConnection(url, user, password);//获取连接
            Log.e(TAG,"连接成功");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }


//        在非主线程中定义Handler=new Hander()会报错，原因是非主线程中默认没有创建Looper对象，需要先
//        调用Looper.prepare()启用Looper。
        Looper.prepare();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.e(TAG,"子线程接收到信息");

                switch (msg.what){
                    case INT_VERIFY_USERNAME_PASSWORD_IF_CORRECT:
                        //进行账号密码校对
//                        if(msg.obj!=null)Log.e(TAG,"1--msg.obj not null");
                        VerifyUsernamePassword(msg);
                        break;
                    case INT_COMPLETE_CHILD_THREAD_HANDLER:
                        Log.e(TAG,"接收handler");
                        if(Mainhandler==null){
                            Mainhandler=(Handler)msg.obj;
                        }
                        break;
                }
                if(connection==null){
                    Mainhandler.obtainMessage(INT_DATABASE_CONNECTION_FAILURE,null);

                    return;
                }
            }

        };
        //        当调用mHandler.getLooper().quit()后，loop才会中止，其后的代码才能得以运行。
        Looper.loop();//开启循环.该句以后的代码将不会被执行


    }


    private void VerifyUsernamePassword(Message messageFromActivity){
        if(messageFromActivity==null)Log.e(TAG,"3--msg is null");
        if(messageFromActivity.obj==null)Log.e(TAG,"3--msg.obj is null");
        String[] stringsUsernameAndPassword=(String[])messageFromActivity.obj;
//        String sql = new String("select * from UsernameAndPassword_table ");
        String sql = new String("select * from UsernameAndPassword_table where username ='"+stringsUsernameAndPassword[0]+"'");

            new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection==null) {
                        connection = DriverManager.getConnection(url, user, password);//获取连接
                        Log.e(TAG,"连接成功");
                    }
                    Log.e(TAG,"进行密码校对......");
                    PreparedStatement statement =  connection.prepareStatement(sql);
                    if(connection==null)
                    Log.e(TAG,"进行密码校对ERROR--Connect is NULL\t");
                    Log.e(TAG,"SQL语句: "+sql);
                    ResultSet rs = statement.executeQuery();
                    if(rs.wasNull()){
                        //不存在该账号
                        Log.e(TAG,"不存在该账号");
                        Mainhandler.obtainMessage(INT_INCORRECT_USERNAME_OR_PASSWORD,null).sendToTarget();
                    }
                    else {
                        rs.next();
                        if(rs.getString(STRING_PASSWORD).equals(stringsUsernameAndPassword[1])) {
                            //账号密码都正确
                            Log.e(TAG,"账号密码都正确");
                            Mainhandler.obtainMessage(INT_CORRECT_USERNAME_PASSWORD,null).sendToTarget();
                        }
                        else{
                            //密码错误
                            Log.e(TAG,"密码错误");
                            Mainhandler.obtainMessage(INT_INCORRECT_USERNAME_OR_PASSWORD,null).sendToTarget();
                        }
                    }
                    Log.e(TAG,"密码校对正常!");

                } catch (SQLException throwables) {
                    Log.e(TAG,"密码校对异常Exception");
                    throwables.printStackTrace();
                }

            }
            }).start();

    }



//
//    /*
//     * 连接数据库
//     * */
//    public  Connection getConnection(){
//
//        new Thread(new Runnable() {
//                        @Override
//            public void run() {
//
////                            Connection conn = null;
////                            try {
////                                Class.forName(diver);
////                                conn = DriverManager.getConnection(url, user, password);//获取连接
////                                Log.e(TAG, "sss");
////                                String sql = "select * from UsernameAndPassword_table";
////                                Statement statement = conn.createStatement();
////                                ResultSet rs = statement.executeQuery(sql);
////                                rs.next();
////                                String usernamess = rs.getString("username");
////                                Log.e("MYSQL", usernamess);
////
////                            } catch (ClassNotFoundException e) {
////                                e.printStackTrace();
////                            } catch (SQLException e) {
////                                Log.e(TAG, e.getMessage());
////                                e.printStackTrace();
////                            }
//
//                            Looper.prepare();//把main thread中自动执行的方法手动执行,为SubThread绑定Looper和消息队列mQueue
//                            looper = Looper.myLooper();//从子线程获取和子线程绑定的looper对象
//                            handler = new Handler(Looper.getMainLooper()) {//这个在子线程中创建的handler是绑定到子线程中的
//                                @Override
//                                public void handleMessage(Message msg) {
//                                    super.handleMessage(msg);
//                                    switch (msg.what){
//                                        case sss:
//                                            Log.e(TAG,"子线程接收到信息");
//                                            handler.obtainMessage(2,"main thread 发送的").sendToTarget();
//                                            break;
//                                    }
//                                   Log.e(TAG,"在subThread中收到了:"+msg.obj);//Toast底层依靠的就是looper环境 因为现在子线程有了looper环境,所以现在子线程也可以弹Toast
//                                }
//                            };
//                            Looper.loop();//手动开启loop()方法用里面的无尽循环阻塞线程 等待接收消息,收到消息后就给handler处理
//                            Log.w("sub Thread", "我执行了子线程就结束了");
//                       //启动Looper循环
//
//                            handler.obtainMessage(1,"main thread 发送的").sendToTarget();
//                        }
//        }).start();
//
//        return null;
//    }
}
