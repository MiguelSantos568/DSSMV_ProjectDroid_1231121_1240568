package com.example.condosched.domain;

public class Booking {
    private long id;
    private long unitId;
    private long amenityId;
    private String amenityName;
    private long residentId;
    private String startTime;
    private String endTime;
    private String status;
    private int numberOfGuests;
    private Double fee = 0.0;
    private String notes;

    public Booking() {
    }

    public Booking(long id, long amenityId, long residentId, String startTime, String endTime, String status,
                   int numberOfGuests, double fee, String notes) {
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

    public long getUnitId() {
        return unitId;
    }

    public void setUnitId(long unitId) {
        this.unitId = unitId;
    }

    public long getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(long amenityId) {
        this.amenityId = amenityId;
    }

    public String getAmenityName() {
        return amenityName;
    }

    public void setAmenityName(String amenityName) {
        this.amenityName = amenityName;
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

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
