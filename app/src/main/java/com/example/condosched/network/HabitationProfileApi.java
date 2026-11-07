package com.example.condosched.network;

import com.example.condosched.dto.HabitationProfileDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HabitationProfileApi {
    @GET("habitation_profiles")
    Call<List<HabitationProfileDTO>> find(@Query("unit_id") String unitFilter);

    @Headers("Prefer: return=representation")
    @POST("habitation_profiles")
    Call<List<HabitationProfileDTO>> create(@Body HabitationProfileDTO profile);
}
