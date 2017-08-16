package cn.kidyn.qdmedical160.util.encrypt;

//利用jni实现对参数的加密，http://m.blog.csdn.net/qq_32306361/article/details/75194739

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