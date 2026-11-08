package com.example.condosched.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clientes independentes para não partilhar chaves entre servidores.
 */
public final class ApiClient {
    private static final String THE_CONDO_URL = "http://193.136.62.24/thecondo/api/";

    // As duas instâncias impedem o envio da chave Supabase para a TheCondo.
    private static final Retrofit THE_CONDO = createTheCondoClient();
    private static final Retrofit SUPABASE = createSupabaseClient();
    private static final Retrofit WEATHER = new Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .client(createHttpClient().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final CondominiumApi CONDOMINIUM = THE_CONDO.create(CondominiumApi.class);
    private static final AmenityApi AMENITIES = THE_CONDO.create(AmenityApi.class);
    private static final BookingApi BOOKINGS = SUPABASE.create(BookingApi.class);
    private static final ResidentApi RESIDENTS = THE_CONDO.create(ResidentApi.class);
    private static final HabitationProfileApi PROFILES = SUPABASE.create(HabitationProfileApi.class);
    private static final WeatherApi FORECAST = WEATHER.create(WeatherApi.class);

    private ApiClient() {
    }

    private static OkHttpClient.Builder createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS);
    }

    private static Retrofit createTheCondoClient() {
        return new Retrofit.Builder()
                .baseUrl(THE_CONDO_URL)
                .client(createHttpClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static Retrofit createSupabaseClient() {
        OkHttpClient client = createHttpClient()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .header("apikey", SupabaseConfig.SUPABASE_KEY)
                        .build()))
                .build();

        return new Retrofit.Builder()
                .baseUrl(SupabaseConfig.REST_URL + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static CondominiumApi condominiums() {
        return CONDOMINIUM;
    }

    public static AmenityApi amenities() {
        return AMENITIES;
    }

    public static BookingApi bookings() {
        return BOOKINGS;
    }

    public static ResidentApi residents() {
        return RESIDENTS;
    }

    public static HabitationProfileApi profiles() {
        return PROFILES;
    }

    public static WeatherApi weather() {
        return FORECAST;
    }
}
