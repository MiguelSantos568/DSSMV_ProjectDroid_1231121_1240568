package com.example.condosched.network;

import com.example.condosched.dto.CondominiumDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CondominiumApi {
    @GET("condominiums")
    Call<List<CondominiumDTO>> getCondominiums();
}
