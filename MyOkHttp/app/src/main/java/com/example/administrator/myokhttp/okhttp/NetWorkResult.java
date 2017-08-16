package com.example.administrator.myokhttp.okhttp;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/14.
 */

public class NetWorkResult implements Serializable{

   private int status;
    private String msg;
    private String data;
    private String request_timestamp;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRequest_timestamp() {
        return request_timestamp;
    }

    public void setRequest_timestamp(String request_timestamp) {
        this.request_timestamp = request_timestamp;
    }
}
