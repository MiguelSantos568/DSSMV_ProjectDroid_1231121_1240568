package com.example.condosched.domain.DTO;

public class AmenityDTO {
    private long id;
    private String name;
    private String description;
    private String type;
    private long buildingId;
    private boolean requiresBooking;
    private int capacity;
    private String openingTime;
    private String closingTime;
    private double bookingFee;
    private boolean isActive;
    private String rules;

    public AmenityDTO() {}

    public AmenityDTO(long id, String name, String description, String type, long buildingId, boolean requiresBooking, int capacity, String openingTime, String closingTime, double bookingFee, boolean isActive, String rules) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.buildingId = buildingId;
        this.requiresBooking = requiresBooking;
        this.capacity = capacity;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.bookingFee = bookingFee;
        this.isActive = isActive;
        this.rules = rules;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBuildingId(long buildingId) {
        this.buildingId = buildingId;
    }

    public void setRequiresBooking(boolean requiresBooking) {
        this.requiresBooking = requiresBooking;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public void setBookingFee(double bookingFee) {
        this.bookingFee = bookingFee;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public long getBuildingId() {
        return buildingId;
    }

    public boolean isRequiresBooking() {
        return requiresBooking;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public double getBookingFee() {
        return bookingFee;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getRules() {
        return rules;
    }
}
