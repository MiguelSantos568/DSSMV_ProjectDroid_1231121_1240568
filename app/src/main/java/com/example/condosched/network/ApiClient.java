package com.example.condosched.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Two independent clients: the Supabase key is never sent to TheCondo. */
public final class ApiClient {
    private ApiClient() {}

    private static OkHttpClient.Builder httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS);
    }

    private static final CondominiumApi CONDOMINIUM = new Retrofit.Builder()
            .baseUrl("http://193.136.62.24/thecondo/api/")
            .client(httpClient().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CondominiumApi.class);

    private static final HabitationProfileApi PROFILES = new Retrofit.Builder()
            .baseUrl(SupabaseClient.REST_URL + "/")
            .client(httpClient().addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                    .header("apikey", SupabaseClient.SUPABASE_KEY).build())).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(HabitationProfileApi.class);

    public static CondominiumApi condominiums() { return CONDOMINIUM; }
    public static HabitationProfileApi profiles() { return PROFILES; }
}
