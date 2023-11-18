package com.picmob.android.mvvm.webservices;

import androidx.annotation.NonNull;

import com.picmob.android.mvvm.utils.AppConstant;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    @NonNull
    public static Retrofit getClient() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(
                new Interceptor() {
                    @NonNull
                    @Override
                    public Response intercept(@NonNull Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder requestBuilder =original.newBuilder().
                                method(original.method(), original.body());
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(new UnauthorizedInterceptor())
                .connectTimeout(30,TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();


        return new Retrofit.Builder().baseUrl(AppConstant.BASE_URL).
                client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create()).build();


      /*  return new Retrofit.Builder().baseUrl(AppConstant.BASE_URL).
                client(new OkHttpClient().newBuilder().addNetworkInterceptor(loggingInterceptor).connectTimeout(30, TimeUnit.SECONDS).
                        readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build())
                .addConverterFactory(GsonConverterFactory.create()).build();*/
    }
}
