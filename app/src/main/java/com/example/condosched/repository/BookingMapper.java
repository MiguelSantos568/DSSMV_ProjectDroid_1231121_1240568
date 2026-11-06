package com.example.condosched.repository;

import com.example.condosched.domain.Booking;
import com.example.condosched.domain.Resident;
import com.example.condosched.dto.BookingDTO;
import com.example.condosched.dto.ResidentDTO;

public final class BookingMapper {
    private BookingMapper() { }

    public static Booking toDomain(BookingDTO dto) {
        Booking booking = new Booking();
        booking.setId(dto.getId() == null ? 0 : dto.getId());
        booking.setUnitId(dto.getUnitId());
        booking.setAmenityId(dto.getAmenityId());
        booking.setResidentId(dto.getResidentId());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setStatus(dto.getStatus());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setFee(dto.getFee());
        booking.setNotes(dto.getNotes());
        return booking;
    }

    public static Resident toDomain(ResidentDTO dto) {
        Resident resident = new Resident();
        resident.setId(dto.getId());
        resident.setUnitId(dto.getUnitId());
        resident.setFirstName(dto.getFirstName());
        resident.setLastName(dto.getLastName());
        resident.setActive(dto.isActive());
        return resident;
    }
}
