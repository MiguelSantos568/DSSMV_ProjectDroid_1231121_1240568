package com.example.condosched.dto;

import java.util.List;

public class BuildingDTO {
    private long id;
    private String name;
    private long condominiumId;
    private List<UnitDTO> units;

    public BuildingDTO() {
    }

    public List<UnitDTO> getUnits() {
        return units;
    }

    public void setUnits(List<UnitDTO> units) {
        this.units = units;
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

    public long getCondominiumId() {
        return condominiumId;
    }

    public void setCondominiumId(long condominiumId) {
        this.condominiumId = condominiumId;
    }
}
