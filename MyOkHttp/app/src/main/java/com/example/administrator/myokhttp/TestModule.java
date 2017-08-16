package com.example.administrator.myokhttp;

import android.content.Context;

import com.example.administrator.myokhttp.okhttp.NetWorkCallBack;
import com.example.administrator.myokhttp.okhttp.NetWorkResult;
import com.example.administrator.myokhttp.okhttp.NetworkWorker;
import com.example.administrator.myokhttp.okhttp.PredefineField;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/15.
 */

public class TestModule {

    Context mContext;
    MyCacheUtils mCacheUtils;//缓存

    public TestModule(Context context) {
        mContext = context;
        mCacheUtils = new MyCacheUtils.Builder().build();
    }

    public void getMemberInfo(int requestStrategy, boolean isShowDialog, final DataCallBack dataCallBack) {
        NetworkWorker.Builder builder = new NetworkWorker.Builder(mContext);
        builder.url("user", "getUserBaseInfo").addParams("", "").post().requestStrategy(requestStrategy);
        if (isShowDialog) {
            builder.showDialog();
        }
        builder.disCache(mCacheUtils);
        builder.disCacheKey("baseInfo");
        builder.disCacheTime(10000);
        builder.callBack(new NetWorkCallBack<NetWorkResult>() {
            @Override
            public void onSuccess(NetWorkResult result, String objectFrom) {
                if (null == result) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(result.getData());
                    Gson gson = new Gson();
                    MemberUserInfoItem item = gson.fromJson(jsonObject.toString(), MemberUserInfoItem.class);
                    if (null != item && null != dataCallBack) {
                        dataCallBack.onSuccess(item, PredefineField.FROM_NETWORK);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String hint) {
                if (null != dataCallBack) {
                    dataCallBack.onError("没有获取到数据~");
                }
            }
        });
        builder.build().excute();
    }


    public void getInfoTabReq(int requestStrategy, String requestTag, boolean isShowLoading, final DataCallBack callback) {
        NetworkWorker.Builder builder = new NetworkWorker.Builder(mContext);
        builder.url("news", "getNewsTag")
                .addParams("city_id", "")
                .requestTag(requestTag)
                .requestStrategy(requestStrategy)
                .post();

        if (isShowLoading) {
            builder.showDialog();
        }

        builder.callBack(new NetWorkCallBack<NetWorkResult>() {
            @Override
            public void onSuccess(NetWorkResult result, String objectFrom) {
                if (null != result) {
                    callback.onSuccess(result, objectFrom);
                }
            }

            @Override
            public void onError(String hint) {
                if (null != callback) {
                    callback.onError(hint);
                }
            }
        }).build().excute();
    }

}
