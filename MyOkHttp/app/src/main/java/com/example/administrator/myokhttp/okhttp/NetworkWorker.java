package com.example.administrator.myokhttp.okhttp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * Created by Administrator on 2017/8/14.
 */

public class NetworkWorker {

    public static int CONNECTTIMEOUT = 30;//连接超时
    public static int READTIMEOUT = 30;//读取超时
    public static int WRITETIMEOUT = 30;//写入超时

    private Context mContext;
    private String mUrl;//请求链接
    private String requestMethod;//请求方法，GET或者POST
    private int requestStrategy;//请求策略
    private String params;//请求参数
    private CacheControl mCacheControl;
    private String mRequestTag;//请求网络的标志
    private Map<String, String> mHeader;//
    private Map<String, String> mFileParams;//上传的文件参数，value为文件路径
    private boolean isShowDialog;//是否展示loading
    private boolean isUICallBack;//是否是主线程
    private NetWorkCallBack<NetWorkResult> mNetWorkCallBack;//网络请求的接口回调
    private static OkHttpClient mOkHttpClient;
    private static Handler mMainHandler;

    public NetworkWorker(Context mContext, String mUrl, String params, Map<String, String> mHeader, boolean isShowDialog, boolean isUICallBack,
                         NetWorkCallBack mCallBack, Map<String, String> fileParams, String requestTag, CacheControl cacheControl, int requestStrategy, String requestMethod) {
        this.mContext = mContext;
        this.mUrl = mUrl;
        this.params = params;
        this.mHeader = mHeader;
        this.isShowDialog = isShowDialog;
        this.isUICallBack = isUICallBack;
        this.mNetWorkCallBack = mCallBack;
        this.mCacheControl = cacheControl;
        this.mFileParams = fileParams;
        this.mRequestTag = requestTag;
        this.requestStrategy = requestStrategy;
        this.requestMethod = requestMethod;
    }

    static {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);//最多4条线程同时请求
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(CONNECTTIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READTIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITETIMEOUT, TimeUnit.MILLISECONDS)
                .followRedirects(false)//不允许重定向
                .followSslRedirects(false)
                .hostnameVerifier(new MyHostNameVerifier())
                .retryOnConnectionFailure(false)//失败不需要重连
                .protocols(Util.immutableList(Protocol.HTTP_1_1));


        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        mMainHandler = new Handler(Looper.getMainLooper());
        mOkHttpClient = builder.build();
    }

    /**
     * 获取get方法传递的参数
     */
    public Request getGetRequest() {
        Request.Builder builder = new Request.Builder();
        getBaseRequest(builder);
        return builder.build();
    }

    /**
     * 获取post方法传递的参数
     */
    public Request getPostRequest() {
        Request.Builder builder = new Request.Builder();
        getBaseRequest(builder);
        builder.post(getRequestBody());
        return builder.build();
    }

    /**
     * 对Request赋基本值
     *
     * @return
     */
    public Request.Builder getBaseRequest(Request.Builder request) {
        request.url(mUrl);
        addHeader(request, mHeader);
        request.cacheControl(mCacheControl);
        request.tag(mRequestTag);
        return request;
    }

    /**
     * 获取RequestBody
     *
     * @return
     */
    private RequestBody getRequestBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM)
                .addFormDataPart("signpush", JNIInterface.encryptFromJNI(params));//添加键值对参数
        if (null != mFileParams) {
            for (Map.Entry<String, String> entry : mFileParams.entrySet()) {
                File file = new File(entry.getValue());
                if (null == file || !file.exists()) {
                    continue;
                }
                builder.addFormDataPart(entry.getKey(), entry.getValue(), RequestBody.create(MediaType.parse("application/octet-stream"), new File(entry.getValue())));
            }
        }
        return builder.build();
    }

    /**
     * 添加header参数
     *
     * @param builder
     * @param headers
     * @return
     */
    private Request.Builder addHeader(Request.Builder builder, Map<String, String> headers) {
        if (null == headers) {
            return builder;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    public void excute(Request request, final NetWorkCallBack mCallBack) {

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (null != mCallBack) {
                    tryErrorCallBack(mCallBack, null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    /**
     * 失败的回调
     *
     * @param callBack
     * @param hint
     */
    public void tryErrorCallBack(final NetWorkCallBack callBack, final String hint) {
        if (isUICallBack) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onError(hint);
                }
            });
        } else {
            callBack.onError(hint);
        }
    }

    /**
     * 成功的回调
     *
     * @param callBack
     * @param result
     */
    public void trySuccessCallBack(final NetWorkCallBack callBack, final NetWorkResult result) {
        if (isUICallBack) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onSuccess(result);
                }
            });
        } else {
            callBack.onSuccess(result);
        }
    }


}
