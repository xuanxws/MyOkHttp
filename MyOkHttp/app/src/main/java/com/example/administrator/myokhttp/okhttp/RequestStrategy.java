package com.example.administrator.myokhttp.okhttp;

/**
 * /请求策略
 * Created by Administrator on 2017/8/14.
 */

public class RequestStrategy {


    public static int GET_SEND_STORE = 1;//先从硬盘中读取数据，然后请求网络，请求到结果后存储到硬盘

    public static int GET_SEND_NO = 2;//先从硬盘中读取数据，然后发送网络请求，但请求到结果后不存储到硬盘

    public static int NO_SEND_NO = 3;//单纯的从网络中读取数据

}
