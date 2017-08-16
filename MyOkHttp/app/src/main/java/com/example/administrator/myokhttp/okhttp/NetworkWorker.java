package com.example.administrator.myokhttp.okhttp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.example.administrator.myokhttp.LoadingDialog;
import com.example.administrator.myokhttp.tools.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import cn.kidyn.qdmedical160.util.encrypt.JNIInterface;
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
import okhttp3.ResponseBody;
import okhttp3.internal.Util;

/**
 * 网络请求的工作者，执行网络封装的地方
 * Created by Administrator on 2017/8/14.
 */

public class NetworkWorker {

    public static int CONNECTTIMEOUT = 30;//连接超时
    public static int READTIMEOUT = 30;//读取超时
    public static int WRITETIMEOUT = 30;//写入超时
    public static final String REQUEST_METHOD_GET = "get";
    public static final String REQUEST_METHOD_POST = "post";
    public static final String baseUrl = "yuming?c=%1$s&a=%2$s&version=" + PredefineField.REQUEST_VERSION;

    private Context mContext;
    private String mUrl;//请求链接
    private String requestMethod;//请求方法，GET或者POST
    private int requestStrategy;//请求策略
    private String params;//请求参数
    private CacheControl mCacheControl;
    private String mRequestTag;//请求网络的标志
    private Map<String, String> mHeader;//
    private NetWorkDisCache mDisCache;
    private String disCacheKey;//缓存的key
    private int validTime;//存储时间
    private Map<String, String> mFileParams;//上传的文件参数，value为文件路径
    private boolean isShowDialog;//是否展示loading
    private boolean isUICallBack;//是否是主线程
    private NetWorkCallBack<NetWorkResult> mNetWorkCallBack;//网络请求的接口回调
    private static OkHttpClient mOkHttpClient;
    private static Handler mMainHandler;
    LoadingDialog mDialog;

    public NetworkWorker(Context mContext, String mUrl, String params, Map<String, String> mHeader, boolean isShowDialog, boolean isUICallBack, NetWorkDisCache netWorkDisCache,
                         String disCacheKey, int validTime, NetWorkCallBack mCallBack, Map<String, String> fileParams, String requestTag, CacheControl cacheControl, int requestStrategy, String requestMethod) {
        this.mContext = mContext;
        this.mUrl = mUrl;
        this.params = params;
        this.mHeader = mHeader;
        this.isShowDialog = isShowDialog;
        this.mDisCache = netWorkDisCache;
        this.isUICallBack = isUICallBack;
        this.disCacheKey = disCacheKey;
        this.validTime = validTime;
        this.mNetWorkCallBack = mCallBack;
        this.mCacheControl = cacheControl;
        this.mFileParams = fileParams;
        this.mRequestTag = requestTag;
        this.requestStrategy = requestStrategy;
        this.requestMethod = requestMethod;
        mDialog = new LoadingDialog(mContext);
    }

    /**
     * 静态块代码，初始化okhttpClient的一些设置
     */
    static {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);//最多4条线程同时请求
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(CONNECTTIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READTIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITETIMEOUT, TimeUnit.SECONDS)
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
     * 外部执行的调用方法
     */
    public void excute() {
        Request request = null;
        if (isShowDialog) {
            mDialog.showLoadingDialog(mContext);
        }
        if (REQUEST_METHOD_GET.equals(requestMethod)) {
            request = getGetRequest();
        } else if (REQUEST_METHOD_POST.equals(requestMethod)) {
            request = getPostRequest();
        }
        excute(request, mNetWorkCallBack);
    }

    /**
     * 根据策略执行网络请求
     *
     * @param request
     * @param netWorkCallBack
     */
    public void excute(Request request, NetWorkCallBack netWorkCallBack) {
        if (requestMethod.equals(REQUEST_METHOD_GET)) {//get方法不响应网络请求策略
            realExcute(request, netWorkCallBack, false);
        }

        switch (requestStrategy) {
            case RequestStrategy.GET_NO_NO:
                useCache(request, netWorkCallBack, false, false);
                return;
            case RequestStrategy.GET_SEND_NO:
                useCache(request, netWorkCallBack, true, false);
                return;
            case RequestStrategy.GET_SEND_STORE:
                useCache(request, netWorkCallBack, true, true);
                return;
            case RequestStrategy.NO_SEND_NO:
                realExcute(request, netWorkCallBack, false);
                return;
            case RequestStrategy.NO_SEND_STORE:
                realExcute(request, netWorkCallBack, true);
                return;
        }
    }

    /**
     * 从硬盘中读取数据
     *
     * @param request
     * @param netWorkCallBack
     */
    public void useCache(final Request request, final NetWorkCallBack netWorkCallBack, final boolean shouldSend, final boolean shouldStore) {
        mOkHttpClient.dispatcher().executorService().submit(new Runnable() {
            @Override
            public void run() {
                tryDismissDialog();
                final NetWorkResult result = mDisCache.get(getDisCacheKey());
                if (isUICallBack) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != result) {
                                trySuccessCallBack(netWorkCallBack, result, PredefineField.FROM_CACHE);
                            }
                            if (shouldSend) {
                                realExcute(request, netWorkCallBack, shouldStore);
                            }
                        }
                    });
                } else {
                    if (null != result) {
                        trySuccessCallBack(netWorkCallBack, result, PredefineField.FROM_CACHE);
                    }
                    if (shouldSend) {//如果需要发送请求，回到主线程发送请求
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                realExcute(request, netWorkCallBack, shouldStore);
                            }
                        });
                    }
                }

            }
        });
    }

    /**
     * 关闭对话框
     */
    private void tryDismissDialog(){
        if (isShowDialog) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                  mDialog.dismissLoadingDialog(mContext);
                }
            });
        }
    }
    /**
     * 获取get方法传递的参数
     */
    public Request getGetRequest() {
        Request.Builder builder = getBaseRequest();
        builder.get();
        return builder.build();
    }

    /**
     * 获取post方法传递的参数
     */
    public Request getPostRequest() {
        Request.Builder builder = getBaseRequest();
        builder.post(getRequestBody());
        return builder.build();
    }

    /**
     * 对Request赋基本值
     *
     * @return
     */
    public Request.Builder getBaseRequest() {
        Request.Builder request = new Request.Builder();
        request.url(mUrl);
        addHeader(request, mHeader);
        setCacheControl(request, mCacheControl);
        setRequestTag(request, mRequestTag);
        return request;
    }

    private Request.Builder setCacheControl(Request.Builder builder, CacheControl cacheControl) {
        builder.cacheControl(cacheControl);
        return builder;
    }

    private Request.Builder setRequestTag(Request.Builder builder, String tag) {
        if (null == tag) {
            return builder;
        }
        builder.tag(tag);
        return builder;
    }

    /**
     * 获取RequestBody
     *
     * @return
     */
    private RequestBody getRequestBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        String value = JNIInterface.encryptFromJNI(params);
        builder.setType(MultipartBody.FORM)
                .addFormDataPart("signpush", value);//添加键值对参数
        if (null != mFileParams) {
            for (Map.Entry<String, String> entry : mFileParams.entrySet()) {
                File file = new File(entry.getValue());
                if (null == file || !file.exists()) {
                    continue;
                }
                builder.addFormDataPart(entry.getKey(), file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), new File(entry.getValue())));
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

    /**
     * 执行网络请求的操作
     *
     * @param request
     * @param mCallBack
     */
    public void realExcute(Request request, final NetWorkCallBack mCallBack, final boolean shouldStore) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                tryDismissDialog();
                if (null != mCallBack) {
                    tryErrorCallBack(mCallBack, null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                tryDismissDialog();
                ResponseBody responseBody = response.body();
                if (null == responseBody) {
                    tryErrorCallBack(mCallBack, null);
                }
                String result = responseBody.string();
                if (null == result) {
                    tryErrorCallBack(mCallBack, null);
                    return;
                }
                result = JNIInterface.decryptFromJNI(result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    NetWorkResult netWorkResult = new NetWorkResult();
                    netWorkResult.setStatus(jsonObject.getInt("status"));
                    netWorkResult.setMsg(jsonObject.getString("msg"));
                    netWorkResult.setRequest_timestamp(jsonObject.getString("request_timestamp"));
                    Object data = jsonObject.getString("data");
                    netWorkResult.setData(null == data ? null : data.toString());
                    if (handleStatus(jsonObject.getInt("status"), jsonObject.getString("msg"), mCallBack)) {
                        if (shouldStore) {
                            if (validTime > 0) {
                                mDisCache.put(getDisCacheKey(), netWorkResult, validTime);
                            } else {
                                mDisCache.put(getDisCacheKey(), netWorkResult);
                            }
                        }
                        trySuccessCallBack(mCallBack, netWorkResult, PredefineField.FROM_NETWORK);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取缓存的key
     *
     * @return
     */
    private String getDisCacheKey() {
        return null == disCacheKey ? mUrl : disCacheKey;
    }

    private boolean handleStatus(int status, String msg, NetWorkCallBack netWorkCallBack) {
        if (status < 0) {
            tryErrorCallBack(netWorkCallBack, msg);
            return false;
        }
        return true;
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
            return;
        }

        if (null != callBack) {
            callBack.onError(hint);
        }
    }

    /**
     * 成功的回调
     *
     * @param callBack
     * @param result
     */
    public void trySuccessCallBack(final NetWorkCallBack callBack, final NetWorkResult result, final String resultFrom) {
        if (isUICallBack) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onSuccess(result, resultFrom);
                }
            });
        } else {
            callBack.onSuccess(result, resultFrom);
        }
    }

    /**
     * 通过builder模式来构建网络参数
     */

    public static class Builder {

        private Context mContext;
        private String mUrl;//请求链接
        private String requestMethod;//请求方法，GET或者POST
        private int requestStrategy;//请求策略
        private String params;//请求参数
        private Map<String, String> mParams;//请求参数集合
        private CacheControl mCacheControl;
        private String mRequestTag;//请求网络的标志
        private Map<String, String> mHeader;//
        private NetWorkDisCache mDisCache;
        private String disCacheKey;//缓存的key
        private int validTime;//存储时间
        private Map<String, String> mFileParams;//上传的文件参数，value为文件路径
        private boolean isShowDialog;//是否展示loading
        private boolean isUICallBack;//是否是主线程
        private NetWorkCallBack<NetWorkResult> mNetWorkCallBack;//网络请求的接口回调

        public Builder(Context context) {
            mContext = context;
        }

        public Builder url(String url) {
            if (null == url) {
                throw new NullPointerException("请求链接不能为空");
            }
            mUrl = url;
            return this;
        }

        public Builder url(String paramC, String paramA) {
            if (null == paramC || null == paramA) {
                throw new NullPointerException("请求链接错误");
            }
            mUrl = String.format(baseUrl, paramC, paramA);
            return this;
        }

        public Builder callBack(NetWorkCallBack<NetWorkResult> mCallBack) {
            mNetWorkCallBack = mCallBack;
            return this;
        }

        public Builder post() {
            requestMethod = REQUEST_METHOD_POST;
            return this;
        }

        public Builder get() {
            requestMethod = REQUEST_METHOD_GET;
            return this;
        }

        public Builder requestStrategy(int strategy) {
            requestStrategy = strategy;
            return this;
        }

        public Builder addFileParams(String keyName, String filePath) {
            if (null == mFileParams) {
                mFileParams = new HashMap<>();
            }
            mFileParams.put(keyName, filePath);
            return this;
        }

        public Builder showDialog() {
            isShowDialog = true;
            return this;
        }

        public Builder mainThread() {
            isUICallBack = true;
            return this;
        }

        public Builder disCacheKey(String key) {
            disCacheKey = key;
            return this;
        }

        public Builder requestTag(String tag) {
            mRequestTag = tag;
            return this;
        }

        public Builder disCache(NetWorkDisCache disCache) {
            mDisCache = disCache;
            return this;
        }

        public Builder disCacheTime(int time) {
            validTime = time;
            return this;
        }

        public Builder addParams(String key, String value) {
            if (null == mParams) {
                mParams = new HashMap<>();
            }
            mParams.put(key, value);
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (null == mHeader) {
                mHeader = new HashMap<>();
            }
            mHeader.put(key, value);
            return this;
        }

        public NetworkWorker build() {
            if (REQUEST_METHOD_POST.equals(requestMethod)) {
                if (null == mParams) {
                    mParams = new HashMap<>();
                }
                mParams.put("f_id", "1049");
                mParams.put("pushtime", String.valueOf(System.currentTimeMillis()));
                mParams.put("version", PredefineField.REQUEST_VERSION);
                mParams.put("city_id", "5");
                mParams.put("UMENG_CHANNEL", PredefineField.UMENG_CHANNEL);
                mParams.put("network_type", NetworkUtils.getNetworkType().name());
                mParams.put("token", PredefineField.TOKEN);
                //mParams.put("access_token", "298941997200167b6f2401e01c906772kxb385mT");
                mParams.put("access_token", "9b168caea4a9da2e7b1daa8c79d7712fEcTdCg3E");
            }
            if (TextUtils.isEmpty(requestMethod)) {//默认为get
                requestMethod = REQUEST_METHOD_GET;
            }
            String param = (null == mParams) ? "" : new JSONObject(mParams).toString();
            if (null == mCacheControl) {
                mCacheControl = CacheControl.FORCE_NETWORK;
            }
            NetworkWorker networkWorker = new NetworkWorker(mContext, mUrl, param, mHeader, isShowDialog, isUICallBack, mDisCache,
                    disCacheKey, validTime, mNetWorkCallBack, mFileParams, mRequestTag, mCacheControl, requestStrategy, requestMethod);
            return networkWorker;
        }
    }


}
