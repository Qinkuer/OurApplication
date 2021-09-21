package com.example.ourapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;


public class MainInterfaceActivity extends AppCompatActivity {
    private static final String TAG = "MainInterfaceActivity"  ;
    private BottomNavigationView bottomNavigation;
    private MapFragment mapFragment=null;
    private OrderFragment orderFragment=null;
    private LeaseFragment leaseFragment=null;
    private PersonalFragment personalFragment=null;
    private FragmentTransaction   supportFragmentTransaction;





    BottomNavigationView.OnItemSelectedListener itemSelectedListener=new BottomNavigationView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //需要重新获取,并且最后还要重新提交,不然就show(Fragment)方法无效.hideAllFragment()也无效
            supportFragmentTransaction=getSupportFragmentManager().beginTransaction();
            hideAllFragment(supportFragmentTransaction);
            switch (item.getItemId()) {
                //这样写会使打开速度变快吧 理论上
                case R.id.page_2_Order:
                   if(orderFragment==null){
                        orderFragment=new OrderFragment();
                        supportFragmentTransaction.add(R.id.frame_layout_Main,orderFragment);
                    }
                    else
                    supportFragmentTransaction.show(orderFragment);

                    break;

                case R.id.page_3_Lease:
                    if(leaseFragment==null){
                        leaseFragment=new LeaseFragment();
                        supportFragmentTransaction.add(R.id.frame_layout_Main,leaseFragment);
                    }
                    else
                    supportFragmentTransaction.show(leaseFragment);
                    break;
                case R.id.page_4_Personal:
                    if(personalFragment==null){
                        personalFragment=new PersonalFragment();
                        supportFragmentTransaction.add(R.id.frame_layout_Main,personalFragment);
                    }
                    else
                    supportFragmentTransaction.show(personalFragment);
                    break;
                //默认是主页
                default:
                    supportFragmentTransaction.show(mapFragment);
                    break;
            }
            supportFragmentTransaction.commit();
            return true;
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);
        init();

    }

    private void init(){
        //由于androidx.fragment.app.Fragment和android.app.Fragment不是同一种.故这里不能用getFragmentManager().beginTransaction()获取FragmentTransaction实例
        //getFragmentManager()获取的是android.app.Fragment
        supportFragmentTransaction=getSupportFragmentManager().beginTransaction();
        mapFragment=new MapFragment();
        supportFragmentTransaction.add(R.id.frame_layout_Main,mapFragment);
        supportFragmentTransaction.commit();
        //先显示主页,或者说默认显示主界面
        hideAllFragment(supportFragmentTransaction);
        supportFragmentTransaction.show(mapFragment);


        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(itemSelectedListener);

    }


    //隐藏所有Fragment
    public void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(mapFragment!=null){
            fragmentTransaction.hide(mapFragment);
        }
        if(orderFragment!=null){
            fragmentTransaction.hide(orderFragment);
        }
        if(leaseFragment!=null){
            fragmentTransaction.hide(leaseFragment);
        }
        if(personalFragment!=null){
            fragmentTransaction.hide(personalFragment);
        }
    }


}