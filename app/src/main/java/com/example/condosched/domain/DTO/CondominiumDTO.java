package com.example.condosched.domain.DTO;

import com.example.condosched.domain.Building;

public class CondominiumDTO {
    private long id;
    private String name;
    private Building buildings;

    public CondominiumDTO(long id, String name, Building buildings) {
        this.id = id;
        this.name = name;
        this.buildings = buildings;
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
}
