/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Generates a configured service for API calls
 * */
public class RevalueServiceGenerator {
    private static final int SECONDS_TIMEOUT = 60;
    private static final String BASE_URL = "http://37.187.240.199:8080/api/";
    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
        .connectTimeout(SECONDS_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(SECONDS_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(SECONDS_TIMEOUT, TimeUnit.SECONDS);

    private static final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());


    public static RevalueServiceContract createService(final String token) {
        if (token != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(RevalueServiceContract.class);
    }
}