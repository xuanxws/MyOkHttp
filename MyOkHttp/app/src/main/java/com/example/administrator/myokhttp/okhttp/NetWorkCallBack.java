package com.example.administrator.myokhttp.okhttp;

/**
 * 网络的接口返回
 * Created by Administrator on 2017/8/14.
 */

public interface  NetWorkCallBack<T> {
    void onSuccess(T result,String objectFrom);

    void onError(String hint);
}
