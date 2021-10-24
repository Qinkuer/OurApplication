package com.example.ourapplication;

import static com.example.ourapplication.HandleMessageWhat.*;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.DOMStringList;

import java.sql.Connection;
import java.util.List;


public class SignIn_UIActivity extends AppCompatActivity {
    private static String TAG="SignIn_UIActivity";
    private TextInputEditText tietUsername;
    private TextInputEditText tietPassword;
    private Button bSignIn;
    private ProgressDialog progressDialog;
    private DB_DataProofreadingLooperThreadClass DB_DataProofreading_LT;

    //用于数据库传递信息
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG,"父线程接收到信息");
            progressDialog.dismiss();
            switch (msg.what){
                case INT_INCORRECT_USERNAME_OR_PASSWORD:
                    Toast.makeText(SignIn_UIActivity.this,"账号或密码错误",Toast.LENGTH_SHORT).show();

                    break;
                case INT_DATABASE_CONNECTION_FAILURE:
                    Toast.makeText(SignIn_UIActivity.this,"服务器连接失败!",Toast.LENGTH_SHORT).show();
                    break;
                case INT_CORRECT_USERNAME_PASSWORD:
                    //进行另一个页面的启动
                    Intent mainUI_intent=new Intent(getApplicationContext(),MainInterfaceActivity.class);
                    mainUI_intent.putExtra("UserName_HavedLoggedIn",tietUsername.getText().toString());
                    startActivity(mainUI_intent);
                    Log.e(TAG,"登录成功");
                    break;
            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signinui);

        init();

//        this.bSignIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).start();

        //启动数据库查询线程线程
//        启动线程
        DB_DataProofreading_LT.start();
    }

    private void init(){
        initProgressDialog();
        tietUsername=findViewById(R.id.InputEidtTextUsername);
        tietPassword=findViewById(R.id.InputEidtTextPassword);
        bSignIn=findViewById(R.id.buttonSignIn);
//        创建数据库查询线程
        DB_DataProofreading_LT=new DB_DataProofreadingLooperThreadClass();

        bSignIn.setOnClickListener(OnClickListnenerOfSignInButton);


    }

    //初始化弹窗
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(SignIn_UIActivity.this);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在登录请稍等...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
    }


    private View.OnClickListener OnClickListnenerOfSignInButton =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            把本活动的Handler传递给子线程,这样子线程就能通过该Handler传递消息给主线程了
            progressDialog.show();
            //服务器连接失败,或者服务器没有开启
            if(DB_DataProofreading_LT.handler==null){
                Toast.makeText(SignIn_UIActivity.this,"服务器连接失败!",Toast.LENGTH_SHORT).show();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
//                            延迟一段时间后关闭提示窗
                            Thread.sleep(1000);
                            progressDialog.dismiss();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();


                return;
            }
            DB_DataProofreading_LT.handler.obtainMessage(INT_COMPLETE_CHILD_THREAD_HANDLER,handler).sendToTarget();
            String[] stringsUsernameAndPassword={tietUsername.getText().toString(),tietPassword.getText().toString()};

            Log.e(TAG,"用户名: "+stringsUsernameAndPassword[0]+"\t密码: "+stringsUsernameAndPassword[1]);
            Message msg=new Message();
            msg.what=INT_VERIFY_USERNAME_PASSWORD_IF_CORRECT;
            msg.obj=stringsUsernameAndPassword;
            //向子线程发送账号和密码
            DB_DataProofreading_LT.handler.sendMessage(msg);
            Log.e(TAG,"发送账号密码成功!");
        }
    };

}