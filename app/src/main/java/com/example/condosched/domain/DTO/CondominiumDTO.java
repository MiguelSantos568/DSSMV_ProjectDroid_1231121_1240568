package com.example.condosched.domain.DTO;

import java.util.List;

public class CondominiumDTO {
    private long id;
    private String name;
    private List<BuildingDTO> buildings;

    public CondominiumDTO() {}

    public CondominiumDTO(long id, String name, List<BuildingDTO> buildings) {
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

    public List<BuildingDTO> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<BuildingDTO> buildings) {
        this.buildings = buildings;
    }
}
