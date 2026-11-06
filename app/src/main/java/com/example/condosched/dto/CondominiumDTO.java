package com.example.condosched.dto;

import java.util.List;

public class CondominiumDTO {
    private long id;
    private String name;
    private List<BuildingDTO> buildings;

    public CondominiumDTO() {
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

    public List<BuildingDTO> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<BuildingDTO> buildings) {
        this.buildings = buildings;
    }
}
