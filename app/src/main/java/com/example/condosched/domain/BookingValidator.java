package com.example.condosched.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class BookingValidator {
    private BookingValidator() {
    }

    public static DataError validate(Amenity amenity, LocalDateTime start, LocalDateTime end,
                                     int people, LocalDateTime now) {
        if (amenity == null || !amenity.isActive()) return DataError.AMENITY_UNAVAILABLE;
        if (start == null || end == null || !start.isAfter(now) || !end.isAfter(start)) {
            return DataError.INVALID_BOOKING_TIME;
        }
        // O formulário marca um dia; fim às 00:00 corresponde à meia-noite seguinte.
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        boolean endsAtNextMidnight = end.toLocalDate().equals(start.toLocalDate().plusDays(1))
                && end.toLocalTime().equals(LocalTime.MIDNIGHT);
        if (!sameDay && !endsAtNextMidnight) return DataError.INVALID_BOOKING_TIME;
        if (people < 1 || (amenity.getCapacity() > 0 && people > amenity.getCapacity())) {
            return DataError.INVALID_GUEST_COUNT;
        }
        LocalTime opening = amenity.getOpeningTime();
        LocalTime closing = amenity.getClosingTime();
        if (opening != null && closing != null && !opening.equals(closing)) {
            LocalDateTime opens = start.toLocalDate().atTime(opening);
            LocalDateTime closes = start.toLocalDate().atTime(closing);
            if (closing.isBefore(opening)) {
                if (start.toLocalTime().isBefore(closing)) opens = opens.minusDays(1);
                else closes = closes.plusDays(1);
            }
            if (start.isBefore(opens) || end.isAfter(closes)) return DataError.OUTSIDE_OPENING_HOURS;
        } else {
            if (opening != null && closing == null && start.toLocalTime().isBefore(opening)) {
                return DataError.OUTSIDE_OPENING_HOURS;
            }
            if (closing != null && opening == null
                    && (end.toLocalTime().isAfter(closing)
                    || !start.toLocalDate().equals(end.toLocalDate()))) {
                return DataError.OUTSIDE_OPENING_HOURS;
            }
        }
        return null;
    }

    public static boolean overlaps(LocalDateTime start, LocalDateTime end,
                                   LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && end.isAfter(otherStart);
    }

    public static DataError validateAvailability(long amenityId, LocalDateTime start,
                                                 LocalDateTime end, List<Booking> bookings) {
        if (bookings == null) return DataError.INVALID_RESPONSE;
        for (Booking booking : bookings) {
            if (booking == null) return DataError.INVALID_RESPONSE;
            if (booking.getAmenityId() != amenityId || isInactive(booking.getStatus())) continue;
            if (booking.getStartTime() == null || booking.getEndTime() == null) {
                return DataError.INVALID_RESPONSE;
            }
            try {
                LocalDateTime otherStart = LocalDateTime.parse(booking.getStartTime());
                LocalDateTime otherEnd = LocalDateTime.parse(booking.getEndTime());
                if (!otherEnd.isAfter(otherStart)) return DataError.INVALID_RESPONSE;
                if (overlaps(start, end, otherStart, otherEnd)) return DataError.BOOKING_CONFLICT;
            } catch (DateTimeParseException error) {
                return DataError.INVALID_RESPONSE;
            }
        }
        return null;
    }

    private static boolean isInactive(String status) {
        if (status == null) return false;
        String value = status.trim();
        return "cancelled".equalsIgnoreCase(value) || "canceled".equalsIgnoreCase(value)
                || "rejected".equalsIgnoreCase(value);
    }
}
