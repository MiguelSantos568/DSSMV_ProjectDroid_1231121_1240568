package com.example.condosched.repository;

import com.example.condosched.domain.Amenity;
import com.example.condosched.dto.AmenityDTO;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * T5: converts the API's transport format into the application's domain object.
 */
public final class AmenityMapper {
    private AmenityMapper() {
    }

    public static Amenity toDomain(AmenityDTO dto, String buildingName) {
        Amenity amenity = new Amenity();
        amenity.setId(dto.getId());
        amenity.setName(dto.getName());
        amenity.setDescription(dto.getDescription());
        amenity.setType(dto.getType());
        amenity.setBuildingId(dto.getBuildingId());
        amenity.setBuildingName(buildingName);
        amenity.setRequiresBooking(dto.isRequiresBooking());
        amenity.setCapacity(dto.getCapacity());
        amenity.setOpeningTime(parseTime(dto.getOpeningTime()));
        amenity.setClosingTime(parseTime(dto.getClosingTime()));
        amenity.setBookingFee(dto.getBookingFee());
        amenity.setActive(dto.isActive());
        amenity.setRules(dto.getRules());
        return amenity;
    }

    private static LocalTime parseTime(String value) {
        if (value != null) {
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException ignored) {
            }
        }
        // Missing or unexpected times remain unknown instead of becoming midnight.
        return null;
    }
}
