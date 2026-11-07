package com.example.condosched.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class BookingValidatorTest {
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
    private final LocalDateTime start = now.plusDays(1);

    private Amenity amenity() {
        Amenity amenity = new Amenity();
        amenity.setId(1);
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

    private Booking booking(LocalDateTime from, LocalDateTime until, int people, String status) {
        Booking booking = new Booking();
        booking.setAmenityId(1);
        booking.setStartTime(from.toString());
        booking.setEndTime(until.toString());
        booking.setNumberOfGuests(people);
        booking.setStatus(status);
        return booking;
    }

    @Test
    public void sharedPeriodAcceptsExactCapacityAndRejectsOnePersonOver() {
        Booking existing = booking(start, start.plusHours(1), 2, "confirmed");
        assertNull(BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 8,
                Collections.singletonList(existing)));
        assertEquals(DataError.BOOKING_CONFLICT,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 9,
                        Collections.singletonList(existing)));
    }

    @Test
    public void adjacentExistingBookingsAreNotAddedAsIfTheyWereSimultaneous() {
        Booking first = booking(start, start.plusHours(1), 6, "confirmed");
        Booking second = booking(start.plusHours(1), start.plusHours(2), 6, "confirmed");
        assertNull(BookingValidator.validateAvailability(amenity(), start, start.plusHours(2), 4,
                Arrays.asList(second, first)));
        assertEquals(DataError.BOOKING_CONFLICT,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(2), 5,
                        Arrays.asList(first, second)));
    }

    @Test
    public void detectsPeakInsideRequestedPeriodAndCountsBookingsAlreadyInProgress() {
        Booking ongoing = booking(start.minusHours(1), start.plusHours(3), 2, "confirmed");
        Booking later = booking(start.plusMinutes(30), start.plusHours(1), 5, "pending");
        assertEquals(DataError.BOOKING_CONFLICT,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(2), 4,
                        Arrays.asList(ongoing, later)));
        assertNull(BookingValidator.validateAvailability(amenity(), start, start.plusHours(2), 3,
                Arrays.asList(ongoing, later)));
    }

    @Test
    public void bookingsEndingAtStartOrStartingAtEndDoNotConsumeCapacity() {
        assertNull(BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 10,
                Arrays.asList(
                        booking(start.minusHours(1), start, 10, "confirmed"),
                        booking(start.plusHours(1), start.plusHours(2), 10, "pending"))));
    }

    @Test
    public void ignoresInactiveBookingsAndOtherSpacesButCountsPendingBookings() {
        Booking otherSpace = booking(start, start.plusHours(1), 10, "confirmed");
        otherSpace.setAmenityId(2);
        assertNull(BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 8,
                Arrays.asList(otherSpace,
                        booking(start, start.plusHours(1), 10, "cancelled"),
                        booking(start, start.plusHours(1), 10, "canceled"),
                        booking(start, start.plusHours(1), 10, "rejected"),
                        booking(start, start.plusHours(1), 2, " PENDING "))));
        assertEquals(DataError.BOOKING_CONFLICT,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 9,
                        Collections.singletonList(booking(start, start.plusHours(1), 2, "pending"))));
    }

    @Test
    public void invalidExistingBookingsAreNotMistakenForFreeCapacity() {
        assertEquals(DataError.INVALID_RESPONSE,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 1, null));
        assertEquals(DataError.INVALID_RESPONSE,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 1,
                        Collections.singletonList(null)));
        for (int guests : new int[]{0, -1}) {
            assertEquals(DataError.INVALID_RESPONSE,
                    BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 1,
                            Collections.singletonList(booking(start, start.plusHours(1), guests, "confirmed"))));
        }
        Booking invalid = booking(start, start.plusHours(1), 2, "confirmed");
        for (String invalidStart : new String[]{null, "invalid", start.plusHours(2).toString()}) {
            invalid.setStartTime(invalidStart);
            assertEquals(DataError.INVALID_RESPONSE,
                    BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 1,
                            Collections.singletonList(invalid)));
        }
    }

    @Test
    public void unknownCapacityKeepsExclusiveBookingInsteadOfInventingALimit() {
        Amenity unknown = amenity();
        unknown.setCapacity(0);
        assertNull(BookingValidator.validateAvailability(unknown, start, start.plusHours(1), 50,
                Collections.emptyList()));
        assertEquals(DataError.BOOKING_CONFLICT,
                BookingValidator.validateAvailability(unknown, start, start.plusHours(1), 1,
                        Collections.singletonList(booking(start, start.plusHours(1), 2, "confirmed"))));
    }

    @Test
    public void largeOccupancyCannotOverflowAndLookAvailable() {
        Amenity large = amenity();
        large.setCapacity(Integer.MAX_VALUE);
        assertEquals(DataError.BOOKING_CONFLICT,
                BookingValidator.validateAvailability(large, start, start.plusHours(1), 1,
                        Arrays.asList(
                                booking(start, start.plusHours(1), Integer.MAX_VALUE, "confirmed"),
                                booking(start, start.plusHours(1), Integer.MAX_VALUE, "confirmed"))));
    }

    @Test
    public void availabilityRejectsInvalidRequestAndAcceptsAnEmptySchedule() {
        assertNull(BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 10,
                Collections.emptyList()));
        assertEquals(DataError.INVALID_GUEST_COUNT,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 0,
                        Collections.emptyList()));
        assertEquals(DataError.INVALID_GUEST_COUNT,
                BookingValidator.validateAvailability(amenity(), start, start.plusHours(1), 11,
                        Collections.emptyList()));
        assertEquals(DataError.INVALID_BOOKING_TIME,
                BookingValidator.validateAvailability(amenity(), start, start, 1, Collections.emptyList()));
        assertEquals(DataError.AMENITY_UNAVAILABLE,
                BookingValidator.validateAvailability(null, start, start.plusHours(1), 1, Collections.emptyList()));
    }
}
