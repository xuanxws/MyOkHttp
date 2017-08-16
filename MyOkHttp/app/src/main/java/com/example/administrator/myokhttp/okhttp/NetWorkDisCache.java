package com.example.administrator.myokhttp.okhttp;

/**
 * 网络的硬盘存储接口，外部调用实现接口
 * Created by Administrator on 2017/8/15.
 */

public interface NetWorkDisCache {

    void put(String key, NetWorkResult netWorkResult, int validTime);

    void put(String key, NetWorkResult netWorkResult);

    NetWorkResult get(String key);

    void clear();
}
