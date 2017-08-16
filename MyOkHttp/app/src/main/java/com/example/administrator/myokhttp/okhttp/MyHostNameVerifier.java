package com.example.administrator.myokhttp.okhttp;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import static com.example.administrator.myokhttp.okhttp.NetworkWorker.baseUrl;

/**
 * Created by Administrator on 2017/8/14.
 */

public class MyHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        try {
            URL url = new URL(baseUrl);
            return null != hostname && hostname.equals(url.getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
