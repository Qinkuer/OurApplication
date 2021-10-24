package com.example.ourapplication;

import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DATABASE_USER_PASSWORD;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_DIVER;
import static com.example.ourapplication.ConnectDataBaseClass.STRING_URL_DATABASE;
import static com.example.ourapplication.HandleMessageWhat.INT_CAR_NUMBER_INPUT_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_CHANGE_FAILED;
import static com.example.ourapplication.HandleMessageWhat.INT_DATABASE_CONNECTION_FAILURE;
import static com.example.ourapplication.HandleMessageWhat.INT_DeleteCarNumber_SUCCESS;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CarNumberStringAdapter extends ArrayAdapter<CarNumberIC> {
    private static final String TAG = "CarNumberStringAdapter";
    private Context mContext;
    private int resourceId;
    private MaterialAlertDialogBuilder MADB=null;
    private String UserName_HavedLoggedIn=null;
    private ProgressDialog progressDialog;
    private Handler PersonalFragmentHandler=null;
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.dismiss();
            switch (msg.what){
                case INT_DeleteCarNumber_SUCCESS:

                    DeleteSuccessful((int)msg.obj);
                    break;
                //未知错误导致修改失败
                case INT_CHANGE_FAILED:
                    Toast.makeText(mContext,"删除失败,请联系管理员!",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public CarNumberStringAdapter(@NonNull Context context, int resource, String UserName_HLI, Handler PersonalFragmentHandler) {
        super(context, resource);
        this.mContext=context;
        this.resourceId=resource;
        this.UserName_HavedLoggedIn=UserName_HLI;
        this.PersonalFragmentHandler=PersonalFragmentHandler;
        initProgressDialog();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String CN_String=getItem(position).CarNumberString;
        View view;
        final ViewHolder vh;
        Log.e(TAG,String.valueOf(position)+"\t"+String.valueOf(getItem(position).CarNumberCount)+"\t"+getItem(position).CarNumberString);
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false );
            vh = new ViewHolder();
            vh.tvCarNumberSting=view.findViewById(R.id.textView_CarNumber_String);
            vh.ivDelectCarNumber=view.findViewById(R.id.imageView_DeleteCarNumber);
            vh.Position= position;
            vh.DB_Figure=getItem(position).CarNumberCount;
            initOnClick(vh,view);
            view.setTag(vh);
        }
        else{
            view=convertView;
            vh = (ViewHolder) view.getTag();
        }
        vh.tvCarNumberSting.setText(CN_String);

        return view;
    }



    private void initOnClick(ViewHolder vh,View view){

//        MADB = new MaterialAlertDialogBuilder(mContext).setTitle("").
//                setMessage(view.getResources().getText(R.string.String_Message_to_delect)).
//                setNegativeButton(view.getResources().getText(R.string.String_Cancel), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                }).setPositiveButton(view.getResources().getText(R.string.String_confirm), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
////                        progressDialog.show();
//                        Log.e(TAG,"删除:"+String.valueOf(vh.Position)+"\t"+String.valueOf(vh.DB_Figure)+"\t"+vh.tvCarNumberSting.getText().toString());
////                        DeleteCarNumberFromDataBase(vh);
//                    }
//        });


        vh.ivDelectCarNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"XX删除:"+String.valueOf(vh.Position)+"\t"+String.valueOf(vh.DB_Figure)+"\t"+vh.tvCarNumberSting.getText().toString());
//        询问是否要删除车牌弹窗
                new MaterialAlertDialogBuilder(mContext).setTitle("").
                        setMessage(view.getResources().getText(R.string.String_Message_to_delect)).
                        setNegativeButton(view.getResources().getText(R.string.String_Cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton(view.getResources().getText(R.string.String_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
//                        Log.e(TAG,"删除:"+String.valueOf(vh.Position)+"\t"+String.valueOf(vh.DB_Figure)+"\t"+vh.tvCarNumberSting.getText().toString());
                        DeleteCarNumberFromDataBase(vh);
                    }
                }).show();
            }
        });
    }

    private void DeleteCarNumberFromDataBase(ViewHolder vh){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
                    String sql = "update UserHavedCarNumbers_table set carnumber"+vh.DB_Figure+"='null' where username ='"+UserName_HavedLoggedIn+"' ";
                    PreparedStatement statement =  connection.prepareStatement(sql);
                    int changeInger= statement.executeUpdate(sql);
                    if(changeInger<1){
                        handler.obtainMessage(INT_CHANGE_FAILED).sendToTarget();
                        return ;
                    }
                    else{
                        handler.obtainMessage(INT_DeleteCarNumber_SUCCESS,vh.Position).sendToTarget();
                        Log.e(TAG,"删除:"+String.valueOf(vh.Position)+"\t"+String.valueOf(vh.DB_Figure)+"\t"+vh.tvCarNumberSting.getText().toString());
                    }
                    connection.close();
                    statement.close();
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void DeleteSuccessful(int position) {
        remove(getItem(position));
        notifyDataSetChanged();
        //删除成功后更新列表
        PersonalFragmentHandler.obtainMessage(INT_CAR_NUMBER_INPUT_SUCCESS).sendToTarget();
    }

    //初始化弹窗
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在删除请稍等...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
    }

    class ViewHolder{
        TextView tvCarNumberSting;
        ImageView ivDelectCarNumber;
        int Position;
        int DB_Figure;//该车牌放在数据库中第x+1列
    }
}
