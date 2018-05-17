package com.greenweather.android.util;

/**
 * Created by Administrator on 2018/5/17.
 */
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil
{
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        /**
         * 利用OkHttp的封装，
         * 调用此方法进行发起http请求，传入地址，并注册一个回调处理服务器的响应
         */
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
