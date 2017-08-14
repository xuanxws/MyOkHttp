package com.example.administrator.myokhttp.okhttp;

public class JNIInterface {
    static{
       //加载库文件
       System.loadLibrary("encryptkey");
    }
    //声明原生函数 参数为String类型 返回类型为String
    public static native String stringFromJNI();

    public static native String encryptFromJNI(String params);

    public static native String decryptFromJNI(String response);
}