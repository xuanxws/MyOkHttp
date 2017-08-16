package com.example.administrator.myokhttp;

import com.example.administrator.myokhttp.okhttp.NetWorkDisCache;
import com.example.administrator.myokhttp.okhttp.NetWorkResult;
import com.example.administrator.myokhttp.tools.CacheUtils;

/**
 * Created by Administrator on 2017/8/16.
 */

public class MyCacheUtils implements NetWorkDisCache {

    public static final String DEFAULT_CACHE_NAME = "my_cache";//默认的缓存文件名字
    public static final int DEFAULT_CACHE_SIZE = 10 * 1024 * 1024;//默认的缓存大小
    public static final int DEFAULT_CACHE_COUNT = 50;//默认的缓存文件数

    private String cacheName;
    private int maxCacheSize;
    private int maxCacheCount;
    private CacheUtils mCacheUtils;


    public MyCacheUtils(Builder builder) {
        cacheName = builder.cacheDirName;
        maxCacheSize = builder.cacheSize;
        maxCacheCount = builder.cacheCount;
        mCacheUtils = CacheUtils.getInstance(null == cacheName ? DEFAULT_CACHE_NAME : cacheName,
                0 == maxCacheSize ? DEFAULT_CACHE_SIZE : maxCacheSize,
                0 == maxCacheSize ? DEFAULT_CACHE_COUNT : maxCacheCount);
    }

    @Override
    public void put(String key, NetWorkResult netWorkResult, int validTime) {
        mCacheUtils.put(key, netWorkResult, validTime);
    }

    @Override
    public void put(String key, NetWorkResult netWorkResult) {
        mCacheUtils.put(key, netWorkResult);
    }

    @Override
    public NetWorkResult get(String key) {
        return (NetWorkResult) mCacheUtils.getSerializable(key);
    }

    @Override
    public void clear() {
        mCacheUtils.clear();
    }

    public static class Builder {
        private String cacheDirName;
        private int cacheSize;
        private int cacheCount;

        public Builder setCacheName(String dir) {
            cacheDirName = dir;
            return this;
        }

        public Builder setCacheSize(int size) {
            cacheSize = size;
            return this;
        }

        public Builder setCacheCount(int count) {
            cacheCount = count;
            return this;
        }

        public MyCacheUtils build() {
            return new MyCacheUtils(this);
        }
    }
}
