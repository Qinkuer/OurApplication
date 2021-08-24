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

}
