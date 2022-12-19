package com.example.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static Retrofit getRetrofit(){


        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://35.239.225.98:443/api/")
                //.baseUrl("http://10.0.2.2:5004/api/")
                .client(okHttpClient)
                .build();

        return  retrofit;
    }
}
