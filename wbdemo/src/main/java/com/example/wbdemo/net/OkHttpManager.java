package com.example.wbdemo.net;


import android.os.Looper;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhoujunyu on 2019/5/17.
 */
public class OkHttpManager {

    private OkHttpClient mOkHttpClient;
    private static OkHttpManager mOkHttpManager;
    private Handler mDelivery;

    private OkHttpManager() {
        mOkHttpClient = new OkHttpClient();
        mDelivery = new Handler(Looper.getMainLooper());
    }

    public static OkHttpManager getInstance(){
        synchronized (OkHttpManager.class){
            if(mOkHttpManager == null){
                mOkHttpManager = new OkHttpManager();
            }
        }
        return mOkHttpManager;
    }


    public void post(String url, Map<String,String> map, Callback callback){
        Request request = BuildPostRequest(url,map);
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    public Request BuildPostRequest(String url,Map<String,String> map) {
        Request request = null;
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), (entry.getValue()));
        }
        request = new Request.Builder().url(url).post(builder.build()).build();

        return request;
    }

}