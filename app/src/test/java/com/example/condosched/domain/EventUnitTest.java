package com.example.condosched.domain;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class EventUnitTest {

    @Test
    public void toBooking_isCorrect() {
        Event eve = new Event("pool party", 0,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31));

        Booking booking = eve.toBooking(eve);

        assertEquals("name:pool party;startDate:2020-01-01;endDate:2020-12-31", booking.getNotes());
    }

    /*@Test
    public void toEvent_isCorrect() {
        // TODO
        Booking booking = new  Booking();
        
        assertEquals(eventName);
    }*/
}
