package com.example.condosched.network;

import com.example.condosched.dto.WeatherDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("forecast.json")
    Call<WeatherDTO> getForecast(@Query("key") String key, @Query("q") String location,
                                 @Query("days") int days, @Query("lang") String language);
}
