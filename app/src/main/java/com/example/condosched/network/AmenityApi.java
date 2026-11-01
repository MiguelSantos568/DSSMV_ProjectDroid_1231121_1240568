package com.example.condosched.network;

import com.example.condosched.domain.DTO.AmenityDTO;
import com.example.condosched.domain.DTO.BuildingDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;

public interface AmenityApi {
    @GET("amenities")
    Call<List<AmenityDTO>> getByCondominium(@Query("condominiumId") long condominiumId);

    @GET("buildings/condominium/{id}")
    Call<List<BuildingDTO>> getBuildingsByCondominium(@Path("id") long condominiumId);
}
