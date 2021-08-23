package com.example.ourapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SignIn_UIActivity extends AppCompatActivity {
    private static String TAG="SignIn_UIActivity";
    private TextInputEditText tietUsername;
    private TextInputEditText tietPassword;
    private Button bSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signinui);

//        this.tietUsername=findViewById(R.id.textUsername);
//        this.tietPassword=findViewById(R.id.textPassword);
//        this.bSignIn=findViewById(R.id.buttonSignIn);
//        this.bSignIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//
//
//
//
//            }
//        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).start();



            DBOpenHelper hellp=new DBOpenHelper();
            Connection con=hellp.getConnection();


    }


    private void sendRequestWithOkHttp(){

                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://www.baidu.com")
                            .build();

                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.e(TAG,responseData);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG,"连接百度失败");
                }


    }
    void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,response);
            }
        });
    }



}