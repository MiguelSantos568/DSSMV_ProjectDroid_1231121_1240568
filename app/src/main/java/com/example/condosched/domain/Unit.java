package com.example.condosched.domain;

public class Unit {
    private long id;
    private String unitNumber;
    private long buildingId;
    private int floor;
    private String type;
    private int area;
    private int bedrooms;
    private int bathrooms;
    private String status;
    private int monthlyFee;
    private int permillage;

    public Unit() {}

    public Unit(long id, String unitNumber, long buildingId, int floor, String type, int area, int bedrooms, int bathrooms, String status, int monthlyFee, int permillage) {
        this.id = id;
        this.unitNumber = unitNumber;
        this.buildingId = buildingId;
        this.floor = floor;
        this.type = type;
        this.area = area;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.status = status;
        this.monthlyFee = monthlyFee;
        this.permillage = permillage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(long buildingId) {
        this.buildingId = buildingId;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(int monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public int getPermillage() {
        return permillage;
    }

    public void setPermillage(int permillage) {
        this.permillage = permillage;
    }


}
