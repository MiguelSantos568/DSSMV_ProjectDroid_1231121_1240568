package com.example.condosched.network;

import com.example.condosched.domain.DTO.HabitationProfileDTO;
import com.example.condosched.domain.DTO.ProfileRequestDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HabitationProfileApi {
    @POST("rpc/get_habitation_profile")
    Call<List<HabitationProfileDTO>> find(@Body ProfileRequestDTO request);

    @POST("rpc/create_habitation_profile")
    Call<List<HabitationProfileDTO>> create(@Body ProfileRequestDTO request);
}
