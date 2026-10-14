package com.example.condosched.domain;

public class Booking {
    private long id;
    private long amenityId;
    private long residentId;
    private String startTime;
    private String endTime;
    private String status;
    private int numberOfGuests;
    private int fee;
    private String notes;

    public Booking() {}

    public Booking(long id, long amenityId, long residentId, String startTime, String endTime, String status,
                   int numberOfGuests, int fee, String notes) {
        this.id = id;
        this.amenityId = amenityId;
        this.residentId = residentId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.numberOfGuests = numberOfGuests;
        this.fee = fee;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(long amenityId) {
        this.amenityId = amenityId;
    }

    public long getResidentId() {
        return residentId;
    }

    public void setResidentId(long residentId) {
        this.residentId = residentId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
