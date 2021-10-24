package com.example.ourapplication;


import static com.example.ourapplication.ConnectDataBaseClass.*;
import static com.example.ourapplication.HandleMessageWhat.INT_CONNECT_FAILED;
import static com.example.ourapplication.HandleMessageWhat.INT_CREAT_ORDER_SUCCESS;
import static com.example.ourapplication.HandleMessageWhat.INT_PARKING_TIME_OUT;
import static com.example.ourapplication.HandleMessageWhat.INT_READ_CARNUMBER_DATABASE_OK;


import android.app.ProgressDialog;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Matrix;

import android.graphics.drawable.Drawable;
import android.location.Address;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";
    private final String DB_longitudeString="longitudeString";//经度
    private final String DB_latitudeString="latitudeString";//维度
    private final String DB_ifBeOccupied="OccupiedBoolean";//该车为是否被用
    private final String DB_MarkerID="markerID";
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private View rootView;
    private LocationClient mLocationClient;
    private FloatingActionButton fabPostioning;
    private GeoCoder geoCoder = null;
    private InputParkingInformationPopupWindow Any_ipipw=null;
    private String UserName_HavedLoggedIn=null;
    private ProgressDialog progressDialog;
    private String addDetailAddress;

    //
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case INT_CREAT_ORDER_SUCCESS:
//                    ChangeMarkerIconToRed((Marker) msg.obj);
                    SetAllMaker();
                    break;
                case INT_PARKING_TIME_OUT:
//                    ChangeMarkerIconToGreen((Marker) msg.obj);
                    SetAllMaker();
                    break;
            }
        }
    };




    public MapFragment(String UserName){
        super();
        this.UserName_HavedLoggedIn=UserName;

    }


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
        geoCoder.destroy();
        super.onDestroy();

    }

    //地图Marker点击事件
    BaiduMap.OnMarkerClickListener BM_OnMarkerClickListener = new BaiduMap.OnMarkerClickListener() {
        //marker被点击时回调的方法
        //若响应点击事件，返回true，否则返回false
        //默认返回false
        @Override
        public boolean onMarkerClick(Marker marker) {
            progressDialog.show();
//            为了能方便和GeoCoder联动
//                        Log.e(TAG,"Marker被点击");


            //从数据库读取的MarkerID就是它的Title
//            Log.e(TAG,marker.getTitle());
//            Log.e(TAG,"Marker被点击");
            Bundle bb= marker.getExtraInfo();
            Any_ipipw = initPopWindow(bb,marker);
            addDetailAddress=bb.getString("detailedAddress");



            //设置反地理编码位置坐标
            ReverseGeoCodeOption op = new ReverseGeoCodeOption();
            LatLng latLng=new LatLng(bb.getDouble("latitudeString"),bb.getDouble("longitudeString"));
            op.location(latLng);
            //发起反地理编码请求(经纬度->地址信息)
            geoCoder.reverseGeoCode(op);
            return true;
        }
    };


    private void init(){
        mMapView = rootView.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        fabPostioning = rootView.findViewById(R.id.floating_action_button_Postioning);
        fabPostioning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnCurrentPosition(0,1000);
            }
        });

        //初始化GerCoder;
        initGeoCoder();
        //初始化弹窗
        initProgressDialog();
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
        option.setScanSpan(1000);//扫描周期
        option.setIsNeedAddress(true);//可选，是否需要地址信息，默认为不需要，即参数为false
        option.setNeedNewVersionRgc(true);//可选，设置是否需要最新版本的地址信息。默认需要，即参数为true

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

        mBaiduMap.setOnMarkerClickListener(BM_OnMarkerClickListener);




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
//            Log.e(TAG,"经纬度:"+String.valueOf(location.getLongitude())+"\t"+String.valueOf(location.getLatitude()));

        }


    }

    //把屏幕移动到你的位置
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
                    //        当地图集层小于18(50米)时,自动放大到集层18
                    if(mBaiduMap.getMapStatus().zoom<18.0f){
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.zoom(18.0f);
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


//    BUG.Marker的点击极限是集层19(20比例尺/米),比这个更小的话,就不可被点击
//    标记出所有停车位
    private void SetAllMaker(){
        Context DB_context=this.getContext();
        SDKInitializer.initialize(this.requireContext());
//需要改 将子进程改成读取一个标记一个,而不是读取完再全部标记
//        可正常读入
//        从数据库中获取所有坐标点
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
//遇到在数据库中读取数据成功,但是不能在百度地图上标记出来的问题
//原因: 百度地图坐标LatLng(维度,经度),而不是经纬度
                    Class.forName(STRING_DIVER);
                    Connection connection = DriverManager.getConnection(STRING_URL_DATABASE, STRING_DATABASE_USER, STRING_DATABASE_USER_PASSWORD);//获取连接
                    Log.e(TAG,"连接成功");
                    String sql = new String("select * from SharedParkingMarkerCoordinate_table ");
                    PreparedStatement statement =  connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    List<OverlayOptions> options = new ArrayList<OverlayOptions>();
                    while(rs.next()){
                        double latitude=Double.valueOf(rs.getString(DB_latitudeString));
                        double longitude=Double.valueOf(rs.getString(DB_longitudeString));
//                        LatLng(维度,经度)
                        LatLng point = new LatLng(latitude, longitude);
                        boolean IfBeOccupied=rs.getBoolean(DB_ifBeOccupied);
                        Bitmap bmm;
                        //获取图标
                        if( !IfBeOccupied){
                            bmm =getBitmap(DB_context,R.mipmap.parking_space,0.7f);
                        }
                        else{
                            bmm =getBitmap(DB_context,R.mipmap.parking_full,0.7f);
                        }
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(bmm);
                        String MarkerTitle=rs.getString(DB_MarkerID);
                        //构建MarkerOption，用于在地图上添加Marker
                        Bundle bundle=new Bundle();
                        bundle.putDouble("latitudeString",latitude);//设置维度
                        bundle.putDouble("longitudeString",longitude);//设置经度
                        bundle.putString("markerID",rs.getString("markerID"));//设置markerID
                        bundle.putString("detailedAddress",rs.getString("detailedAddress"));//设置详细地址
                        bundle.putString("username",UserName_HavedLoggedIn);
                        bundle.putBoolean(DB_ifBeOccupied,IfBeOccupied);
                        bundle.putString("OrderID",rs.getString("OrderID"));
                        OverlayOptions option = new MarkerOptions()
                                .position(point)
                                .icon(bitmap)
                                .extraInfo(bundle)
                                .title(MarkerTitle);

                        options.add(option);
                        Log.e(TAG,"生成一个标记 经度:"+rs.getString(DB_longitudeString)+"\t维度:"+rs.getString(DB_latitudeString));
                    };
                    mBaiduMap.addOverlays(options);
                    connection.close();
                    statement.close();
                } catch (ClassNotFoundException | SQLException e) {
                    Log.e(TAG,"反正就是失败了");
                    e.printStackTrace();
                }


            }
        }.start();

//        //定义Maker坐标点
//        LatLng point = new LatLng(37.421998, -122.084);
////构建Marker图标
//        Bitmap bmm =getBitmap(this.requireContext(),R.mipmap.parking_space,0.7f);
//
//        BitmapDescriptor bitmap = BitmapDescriptorFactory
//                .fromBitmap(bmm);
//
//
//        Bundle bundle=new Bundle();
//        bundle.putDouble("latitudeString",37.421998);
//        bundle.putDouble("longitudeString",-122.084);
//
////构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions option = new MarkerOptions()
//                .position(point)
//                .extraInfo(bundle)
////                .extraInfo(new Bundle().putBundle("markerID",))
//                .icon(bitmap);
//
////在地图上添加Marker，并显示
//
//        mBaiduMap.addOverlay(option);
    }



//    因为版本的原因,不能使用:
//Bitmap bmm = BitmapFactory. decodeResource(getResources(),R.drawable.ic_baseline_local_parking_24);
//    只能用下面的方法
//    第三个参数为放缩比例
    private static Bitmap getBitmap(Context context,int vectorDrawableId,float scaling) {
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
// 获得图片的宽高
        int width = bitmap_101.getWidth();
        int height = bitmap_101.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scaling,  scaling);
        Bitmap newbm = Bitmap.createBitmap(bitmap_101, 0, 0, width, height, matrix, true);
        return newbm;
    }


    private InputParkingInformationPopupWindow initPopWindow(Bundle information,Marker marker){
        InputParkingInformationPopupWindow IPIPopupWindow = new InputParkingInformationPopupWindow(requireActivity(),information,marker,handler);
//        Log.e(TAG,"1");
        if(rootView!=null)
        IPIPopupWindow.showAtLocation(rootView.findViewById(R.id.constraintLayoutOfMapFragment), Gravity.CENTER,0,0);
//        Log.e(TAG,"2");
        return IPIPopupWindow;
    }

    private void initGeoCoder(){

//        地理编码&反地理编码
        //不知道为什么它定义的地方不能在BaiduMap.OnMarkerClickListener里面,不然我们设置的方法将无法使用.
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

//            当输入经纬度时,获取地址
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
                //有时候地址会为空
                if(arg0!=null&&arg0.getAddress()!=null){
                    Toast.makeText(requireActivity(),arg0.getAddress(),Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.dismiss();
                    return;
                }
                if(Any_ipipw!=null){
                    Any_ipipw.setAddressInformation(arg0.getAddress()+"\n"+addDetailAddress);
                }
                //获取点击的坐标的地址
                Log.e(TAG,arg0.getAddress());
                progressDialog.dismiss();
            }

//            当输入地址时,获取经纬度
            @Override
            public void onGetGeoCodeResult(GeoCodeResult arg0) {
            }
        });

    }

    //初始化弹窗
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在获取信息请稍等...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
    }

//当车位被占用后
    private void ChangeMarkerIconToRed(Marker marker){
        Bitmap bmm;
        marker.getExtraInfo().putBoolean(DB_ifBeOccupied,true);
        bmm =getBitmap(getContext(),R.mipmap.parking_full,0.7f);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(bmm);
        marker.setIcon(bitmap);
    }

    //车位占用时间结束
    private void ChangeMarkerIconToGreen(Marker marker){
        Bitmap bmm;
        bmm =getBitmap(getContext(),R.mipmap.parking_space,0.7f);
        marker.getExtraInfo().putBoolean(DB_ifBeOccupied,false);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(bmm);
        marker.setIcon(bitmap);
    }


}
