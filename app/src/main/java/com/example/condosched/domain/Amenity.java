package com.example.condosched.domain;

import java.io.Serializable;
import java.time.LocalTime;

public class Amenity implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String name;
    private String description;
    private String type;
    private long buildingId;
    private String buildingName;
    private boolean requiresBooking;
    private int capacity;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Double bookingFee;
    private boolean isActive;
    private String rules;

    public Amenity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(long buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public boolean isRequiresBooking() {
        return requiresBooking;
    }

    public void setRequiresBooking(boolean requiresBooking) {
        this.requiresBooking = requiresBooking;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public Double getBookingFee() {
        return bookingFee;
    }

    public void setBookingFee(Double bookingFee) {
        this.bookingFee = bookingFee;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }
}
