package com.example.condosched.network;

import com.example.condosched.dto.ResidentDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ResidentApi {
    @GET("residents/active")
    Call<List<ResidentDTO>> getActiveResidents();
}
