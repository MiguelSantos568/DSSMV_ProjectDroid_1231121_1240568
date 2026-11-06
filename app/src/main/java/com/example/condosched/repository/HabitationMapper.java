package com.example.condosched.repository;

import com.example.condosched.domain.Habitation;
import com.example.condosched.dto.BuildingDTO;
import com.example.condosched.dto.CondominiumDTO;
import com.example.condosched.dto.UnitDTO;

import java.util.List;

public final class HabitationMapper {
    private HabitationMapper() {
    }

    /**
     * Converts the API hierarchy into the home shown on screen.
     */
    public static Habitation findIn(List<CondominiumDTO> condos, long unitId) {
        for (CondominiumDTO condo : condos) {
            if (condo == null || condo.getBuildings() == null) continue;
            for (BuildingDTO building : condo.getBuildings()) {
                if (building == null || building.getUnits() == null) continue;
                for (UnitDTO unit : building.getUnits()) {
                    if (unit != null && unit.getId() == unitId) {
                        return new Habitation(unitId, condo.getId(), building.getId(), condo.getName(), building.getName(),
                                String.valueOf(unit.getFloor()), unit.getUnitNumber());
                    }
                }
            }
        }
        return null;
    }
}
