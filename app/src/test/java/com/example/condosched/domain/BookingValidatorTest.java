package com.example.condosched.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.Test;
import static org.junit.Assert.*;

public class BookingValidatorTest {
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
    private final LocalDateTime start = now.plusDays(1);

    private Amenity amenity() {
        Amenity amenity = new Amenity();
        amenity.setActive(true);
        amenity.setCapacity(10);
        amenity.setOpeningTime(LocalTime.of(8, 0));
        amenity.setClosingTime(LocalTime.of(20, 0));
        return amenity;
    }

    @Test
    public void acceptsOpeningAndClosingBoundaries() {
        assertNull(BookingValidator.validate(amenity(), start.withHour(8), start.withHour(20), 10, now));
    }

    @Test
    public void rejectsPastAndReversedAndMultiDayPeriods() {
        assertEquals(DataError.INVALID_BOOKING_TIME, BookingValidator.validate(amenity(), now, start, 1, now));
        assertEquals(DataError.INVALID_BOOKING_TIME, BookingValidator.validate(amenity(), start, start, 1, now));
        assertEquals(DataError.INVALID_BOOKING_TIME, BookingValidator.validate(amenity(), start, start.minusHours(1), 1, now));
        assertEquals(DataError.INVALID_BOOKING_TIME, BookingValidator.validate(amenity(), start, start.plusDays(1), 1, now));
    }

    @Test
    public void rejectsInvalidCapacityAndInactiveAmenity() {
        assertEquals(DataError.INVALID_GUEST_COUNT, BookingValidator.validate(amenity(), start, start.plusHours(1), 0, now));
        assertEquals(DataError.INVALID_GUEST_COUNT, BookingValidator.validate(amenity(), start, start.plusHours(1), 11, now));
        Amenity inactive = amenity();
        inactive.setActive(false);
        assertEquals(DataError.AMENITY_UNAVAILABLE, BookingValidator.validate(inactive, start, start.plusHours(1), 1, now));
    }

    @Test
    public void rejectsPeriodsOutsideOpeningHours() {
        assertEquals(DataError.OUTSIDE_OPENING_HOURS, BookingValidator.validate(amenity(), start.withHour(7), start, 1, now));
        assertEquals(DataError.OUTSIDE_OPENING_HOURS, BookingValidator.validate(amenity(), start, start.withHour(21), 1, now));
    }

    @Test
    public void allowsUnknownCapacityAndHoursWithoutInventingLimits() {
        Amenity amenity = amenity();
        amenity.setCapacity(0);
        amenity.setOpeningTime(null);
        amenity.setClosingTime(null);
        assertNull(BookingValidator.validate(amenity, start, start.plusHours(1), 50, now));
    }

    @Test
    public void handlesOvernightOpeningAndMidnightEnd() {
        Amenity amenity = amenity();
        amenity.setOpeningTime(LocalTime.of(22, 0));
        amenity.setClosingTime(LocalTime.of(2, 0));
        assertNull(BookingValidator.validate(amenity, start.withHour(23), start.plusDays(1).withHour(0), 1, now));
        assertNull(BookingValidator.validate(amenity, start.withHour(0), start.withHour(2), 1, now));
        assertEquals(DataError.OUTSIDE_OPENING_HOURS,
                BookingValidator.validate(amenity, start.withHour(3), start.withHour(4), 1, now));
    }

    @Test
    public void adjacentBookingsDoNotOverlapButPartialAndContainingPeriodsDo() {
        assertFalse(BookingValidator.overlaps(start, start.plusHours(1), start.plusHours(1), start.plusHours(2)));
        assertTrue(BookingValidator.overlaps(start, start.plusHours(1), start.plusMinutes(30), start.plusHours(2)));
        assertTrue(BookingValidator.overlaps(start, start.plusHours(2), start.plusMinutes(30), start.plusHours(1)));
        assertTrue(BookingValidator.overlaps(start, start.plusHours(1), start.minusHours(1), start.plusHours(2)));
    }
}
