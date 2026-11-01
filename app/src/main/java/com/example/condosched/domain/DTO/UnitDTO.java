package com.example.condosched.domain.DTO;

public class UnitDTO {
    private long id;
    private String unitNumber;
    private long buildingId;
    private int floor;
    private String type;
    private double area;
    private int bedrooms;
    private int bathrooms;
    private String status;
    private double monthlyFee;
    private double permillage;

    public UnitDTO() {}

    public UnitDTO(long id, String unitNumber, long buildingId, int floor, String type, double area, int bedrooms, int bathrooms, String status, double monthlyFee, double permillage) {
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

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
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

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public double getPermillage() {
        return permillage;
    }

    public void setPermillage(double permillage) {
        this.permillage = permillage;
    }
}
