package com.example.condosched.dto;

import com.google.gson.annotations.SerializedName;

public class CancelBookingRequestDTO {
    @SerializedName("p_unit_id")
    private long unitId;
    @SerializedName("p_booking_id")
    private long bookingId;

    public CancelBookingRequestDTO() {
    }

    public long getUnitId() {
        return unitId;
    }

    public void setUnitId(long unitId) {
        this.unitId = unitId;
    }

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }
}
