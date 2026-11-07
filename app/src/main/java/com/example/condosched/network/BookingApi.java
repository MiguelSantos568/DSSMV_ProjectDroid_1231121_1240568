package com.example.condosched.network;

import com.example.condosched.dto.BookingDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BookingApi {
    @GET("bookings")
    Call<List<BookingDTO>> getForHome(@Query("unit_id") String unitFilter);

    @GET("bookings?select=amenity_id,start_time,end_time,status&status=in.(confirmed,pending)")
    Call<List<BookingDTO>> getForAmenity(@Query("amenity_id") String amenityFilter);

    @Headers("Prefer: return=representation")
    @POST("bookings")
    Call<List<BookingDTO>> create(@Body BookingDTO booking);

    @Headers("Prefer: return=representation")
    @DELETE("bookings?select=id,unit_id")
    Call<List<BookingDTO>> deleteBooking(@Query("id") String bookingFilter,
                                       @Query("unit_id") String unitFilter);
}
