package com.example.administrator.myokhttp.okhttp;

/**
 * Created by Administrator on 2017/8/14.
 */

public class NetWorkResult {

   private int status;
    private String msg;
    private String data;
    private String request_timesmap;

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

    public String getRequest_timesmap() {
        return request_timesmap;
    }

    public void setRequest_timesmap(String request_timesmap) {
        this.request_timesmap = request_timesmap;
    }
}
