package com.example.ourapplication;

public class HandleMessageWhat {
    private HandleMessageWhat(){}
    //校对密码和账号是否正确
    public static final int INT_VERIFY_USERNAME_PASSWORD_IF_CORRECT=3511;
    //账号存在数据库里面的列名
    public static final String STRING_USERNAME="username";
    //密码存在数据库里面的列名
    public static final String STRING_PASSWORD="password";
    //将主线程的handler传递给子线程
    public static final int INT_COMPLETE_CHILD_THREAD_HANDLER=1244124;
    //账号密码都正确
    public static final int INT_CORRECT_USERNAME_PASSWORD=46345;
    //账号或密码错误
    public static final int INT_INCORRECT_USERNAME_OR_PASSWORD=89274;
    //数据库连接失败
    public static final int INT_DATABASE_CONNECTION_FAILURE=958613;
    //数据库数据库查询完成
    public static final int INT_READ_CARNUMBER_DATABASE_OK=846873;
    //车牌号插入成功
    public static final int INT_CAR_NUMBER_INPUT_SUCCESS=354745;
    //服务器连接失败,或者数据库没有打开
    public static final int INT_CONNECT_FAILED=5568168;
    //允许修改密码,输入的旧密码正确
    public static final int INT_ALLOW_CHANGE_PASSWORD=91315;
    //不允许修改密码,因为输入的旧密码错误
    public static final int INT_NOT_ALLOW_CHANGE_PASSWORD=91318;
    //完成修改旧密码
    public static final int INT_CHANGE_PASSWORD_SUCCESS=81123;
    //未知错误导致修改失败
    public static final int INT_CHANGE_FAILED=111111;
    //删除车牌号成功
    public static final int INT_DeleteCarNumber_SUCCESS=5463;
    //生成订单成功
    public static final int INT_CREAT_ORDER_SUCCESS=35153;
    //获取订单信息
    public static final int INT_GET_INFORMATION_OF_ORDER=65813;
    //车位到期了
    public static final int INT_PARKING_TIME_OUT=65003;
//获取订单完成
    public static final int INT_GET_ALL_ORDER_SUCCCESS=602703;
    //获取用户拥有的车位完成
    public static final int INT_GET_USER_ALL_PARKING_SUCCCESS=602833;
}
