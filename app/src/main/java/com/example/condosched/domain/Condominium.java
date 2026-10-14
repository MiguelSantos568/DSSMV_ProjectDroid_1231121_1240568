package com.example.condosched.domain;

public class Condominium {
    private long id;
    private String name;
    private Building buildings;

    private Coordinates coordinates;

    public Condominium() {}

    public Condominium(long id, String name, Building buildings, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.buildings = buildings;
        this.coordinates = new Coordinates(latitude, longitude);
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

    public Building getBuildings() {
        return buildings;
    }

    public void setBuildings(Building buildings) {
        this.buildings = buildings;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void addCoordinatesToName() {
        Coordinates coordinates = this.getCoordinates();
        String notatedCoord = coordinates.toNotatedText(coordinates);
        this.name =  this.name + ";" + notatedCoord;
    }
}
