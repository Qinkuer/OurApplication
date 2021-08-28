package com.example.ourapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;

import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;


public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private View rootView;
    private LocationClient mLocationClient;
    private ImageView imageViewPostioning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.map_fragment_layout, container, false);

        init();
        return rootView;
    }
    @Override
    public void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        super.onResume();

    }
    @Override
    public void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();

    }
    @Override
    public void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();

    }




    private void init(){
        mMapView = rootView.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        imageViewPostioning=rootView.findViewById(R.id.imageView_Postioning);
        imageViewPostioning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnCurrentPosition(0,1000);
            }
        });

//        设置地图缩放为50米
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

//开启交通图
        mBaiduMap.setTrafficEnabled(true);
//        开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //定位初始化
        mLocationClient = new LocationClient(this.requireContext());

//通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        option.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
//设置locationClientOption
        mLocationClient.setLocOption(option);

//注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
//开启地图定位图层
        mLocationClient.start();

        ReturnCurrentPosition(5000,1000);
        SetAllMaker();
    }

    //构造地图数据
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
//            Log.e(TAG,"经纬度:"+String.valueOf(location.getLatitude())+"\t"+String.valueOf(location.getLongitude()));
        }


    }

    public void ReturnCurrentPosition(int DelayTime1 ,int DelayTime2){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(DelayTime1);
                    MyLocationConfiguration mMyLocationConfiguration=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,false, BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_list_24));
                    mBaiduMap.setMyLocationConfiguration(mMyLocationConfiguration);
                    Thread.sleep(DelayTime2);
                    mMyLocationConfiguration=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,false, BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_list_24));
                    mBaiduMap.setMyLocationConfiguration(mMyLocationConfiguration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void SetAllMaker(){
        SDKInitializer.initialize(this.requireContext());
        //定义Maker坐标点
        LatLng point = new LatLng(37.421998, -122.184);
//构建Marker图标

//        Bitmap bmm = BitmapFactory. decodeResource(getResources(),R.drawable.ic_baseline_local_parking_24);
//        Bitmap bmm = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/parkingspace.png"));
        Bitmap bmm =getBitmap(this.requireContext(),R.mipmap.parkingspace);
        if(bmm==null){
            Log.e(TAG,"转换成Bitmap失败");
        }
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromBitmap(bmm);
                
        if(bitmap==null){
            Log.e(TAG,"停车位图片获取失败!");
            return;
        }
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
//在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }



//    a
    private static Bitmap getBitmap(Context context,int vectorDrawableId) {
        Bitmap bitmap_101=null;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap_101 = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap_101);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }else {
            bitmap_101 = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap_101;
    }

}
