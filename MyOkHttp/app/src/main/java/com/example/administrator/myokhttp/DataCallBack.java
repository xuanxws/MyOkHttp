package com.example.administrator.myokhttp;

/**
 * Created by Administrator on 2017/8/15.
 */

public interface DataCallBack {

    void onSuccess(Object object,String objectFrom);
    void onError(String hint);
}
