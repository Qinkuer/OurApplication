package com.example.ourapplication;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBOpenHelper {
    private static String diver = "com.mysql.jdbc.Driver";
    private static String TAG="DBOpenHelper";
    //加入utf-8是为了后面往表中输入中文，表中不会出现乱码的情况
    private static String url = "jdbc:mysql://116.63.172.25:3306/SharedParking_db";
//    private static String url = "jdbc:mysql://116.63.172.25:3306/SharedParking_db?serverTimezone=GMT%2B8&useSSL=FALSE";
    private static String user = "user_sharedparking";//用户名
    private static String password = "123456";//密码
    /*
     * 连接数据库
     * */
    public static Connection getConnection(){
        new Thread(new Runnable() {
                        @Override
            public void run() {
                            Connection conn = null;
                            try {
                                Class.forName(diver);
                                conn = DriverManager.getConnection(url,user,password);//获取连接
                                Log.e(TAG,"sss");
                                String sql="select * from UsernameAndPassword_table";
                                Statement statement=conn.createStatement();
                                ResultSet rs= statement.executeQuery(sql);
                                rs.next();
                                String usernamess=rs.getString("username");
                                Log.e("MYSQL",usernamess);

                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                Log.e(TAG,e.getMessage());
                                e.printStackTrace();
                            }
            }
        }).start();

        return null;
    }
}
