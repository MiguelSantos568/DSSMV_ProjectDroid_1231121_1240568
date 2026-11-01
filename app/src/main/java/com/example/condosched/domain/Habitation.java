package com.example.condosched.domain;

import java.io.Serializable;

public final class Habitation implements Serializable {
    private static final long serialVersionUID = 1L;
    public final long unitId;
    public final String condominium, building, floor, unit;

    public Habitation(long unitId, String condominium, String building, String floor, String unit) {
        this.unitId = unitId;
        this.condominium = condominium;
        this.building = building;
        this.floor = floor;
        this.unit = unit;
    }
}
