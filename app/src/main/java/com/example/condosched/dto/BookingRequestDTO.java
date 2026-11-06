package com.example.condosched.dto;

import com.google.gson.annotations.SerializedName;

public class BookingRequestDTO {
    @SerializedName("p_booking")
    private BookingDTO booking;

    public BookingRequestDTO() {
    }

    public BookingDTO getBooking() {
        return booking;
    }

    public void setBooking(BookingDTO booking) {
        this.booking = booking;
    }
}
