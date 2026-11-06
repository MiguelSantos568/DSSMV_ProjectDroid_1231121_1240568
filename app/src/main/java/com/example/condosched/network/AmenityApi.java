package com.example.condosched.network;

import com.example.condosched.dto.AmenityDTO;
import com.example.condosched.dto.BuildingDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;

public interface AmenityApi {
    @GET("amenities")
    Call<List<AmenityDTO>> getAll();

    @GET("amenities/{id}")
    Call<AmenityDTO> getById(@Path("id") long id);

    @GET("amenities")
    Call<List<AmenityDTO>> getByCondominium(@Query("condominiumId") long condominiumId);

    @GET("buildings/condominium/{id}")
    Call<List<BuildingDTO>> getBuildingsByCondominium(@Path("id") long condominiumId);
}
