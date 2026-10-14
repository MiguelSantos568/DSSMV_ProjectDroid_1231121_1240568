package com.example.condosched.domain;

import java.time.LocalDate;

public class Event extends Booking{
    private String eventName;
    private long BookingId;
    private LocalDate startDate; //yyyy-mm-dd
    private LocalDate endDate; //yyyy-mm-dd

    public Event(){}

    public Event(long id, long amenityId, long residentId, String startTime, String endTime, String status,
                 int numberOfGuests, int fee, String notes, String eventName, long bookingId, LocalDate startDate,
                 LocalDate endDate) {
        super(id, amenityId, residentId, startTime, endTime, status, numberOfGuests, fee, notes);
        this.eventName = eventName;
        BookingId = bookingId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Event(String eventName, long BookingId, LocalDate startDate, LocalDate endDate) {
        this.eventName = eventName;
        this.BookingId = BookingId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public long getBookingId() {
        return BookingId;
    }

    public void setBookingId(long bookingId) {
        BookingId = bookingId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // method to store extra information of an event into a booking object
    public Booking toBooking(Event event){
        String note = new String("name:" + eventName + ";" + "startDate:" + startDate.toString() + ";" +
                "endDate:" + endDate.toString());
        Booking booking = new Booking(event.BookingId, event.getAmenityId(), event.getResidentId(),
                event.getStartTime(), event.getEndTime(), event.getStatus(), event.getNumberOfGuests(),
                event.getFee(), note);
        return booking;
    }

    public Event toEvent(Booking booking){
        String notes = booking.getNotes();

        int firstSemiCollon = notes.indexOf(";");
        int secondSemiCollon = notes.indexOf(";", firstSemiCollon + 1);

        String name = "name:"; String strStartDate = "startDate:"; String strEndDate = "endDate:";

        String eventName = notes.substring(name.length(), firstSemiCollon - 1);
        LocalDate startDate = LocalDate.parse(notes.substring(firstSemiCollon  + 1, secondSemiCollon - 1));
        LocalDate endDate   = LocalDate.parse(notes.substring(secondSemiCollon + 1, notes.length()));

        long BookingId = booking.getId();

        Event event = new Event(booking.getId(), booking.getAmenityId(), booking.getResidentId(),
                booking.getStartTime(), booking.getEndTime(), booking.getStatus(), booking.getNumberOfGuests(),
                booking.getFee(), booking.getNotes(), eventName, BookingId, startDate, endDate);

        return event;
    }
}
