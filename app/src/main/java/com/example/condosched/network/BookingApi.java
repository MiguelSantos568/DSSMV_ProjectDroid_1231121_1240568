package com.example.condosched.network;

import com.example.condosched.dto.BookingDTO;
import com.example.condosched.dto.BookingRequestDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BookingApi {
    @GET("rpc/get_home_bookings")
    Call<List<BookingDTO>> getForHome(@Query("p_unit_id") long unitId);

    @GET("rpc/get_amenity_bookings")
    Call<List<BookingDTO>> getForAmenity(@Query("p_amenity_id") long amenityId);

    @POST("rpc/create_booking")
    Call<List<BookingDTO>> create(@Body BookingRequestDTO request);
}
