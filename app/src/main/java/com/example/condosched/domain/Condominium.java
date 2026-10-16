
package com.example.condosched.domain;

import java.util.List;

public class Condominium {

    private long id;
    private String name;
    private Building buildings;
    private List<Building> buildingsList;
    private Coordinates coordinates;

    public Condominium() {}

    public Condominium(long id, String name, Building buildings,
                       double latitude, double longitude) {
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

    public List<Building> getBuildingsList() {
        return buildingsList;
    }

    public void setBuildingsList(List<Building> buildingsList) {
        this.buildingsList = buildingsList;
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
        this.name = this.name + ";" + notatedCoord;
    }
}
