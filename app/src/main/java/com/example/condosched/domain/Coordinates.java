package com.example.condosched.domain;

public class Coordinates {

    // atributes
    private double latitude;
    private double longitude;

    // constructors
    public Coordinates() {}
    public Coordinates(double latitude, double longitude) {
        if(latitude < 0 || latitude > 90){
            throw new IllegalArgumentException("Latitude must be between 0 and 90");
        }else{this.latitude = latitude;}
        if(longitude < 0 || longitude > 180){
            throw new IllegalArgumentException("Longitude must be between 0 and 180");
        }else{this.longitude = longitude;}

    }

    // getters and setters
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    // other methods
    public double distanceTo(Coordinates coordinates){
        double deltaLatitude = this.latitude - coordinates.latitude;
        double deltaLongitude = this.longitude - coordinates.longitude;

        return Math.sqrt(Math.pow(deltaLatitude, (double) 2) + Math.pow(deltaLongitude, (double) 2));
    }

}
