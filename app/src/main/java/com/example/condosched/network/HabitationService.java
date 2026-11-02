package com.example.condosched.network;

import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.DTO.BuildingDTO;
import com.example.condosched.domain.DTO.CondominiumDTO;
import com.example.condosched.domain.DTO.UnitDTO;
import java.util.List;

public final class HabitationService {
    public interface Callback {
        void onSuccess(Habitation habitation);
        void onError(String message);
    }

    public void find(long unitId, Callback callback) {
        new CondominiumService().getCondominiums(new CondominiumService.CondominiumCallback() {
            @Override public void onSuccess(List<CondominiumDTO> condos) {
                Habitation home = findIn(condos, unitId);
                if (home != null) callback.onSuccess(home);
                else callback.onError("Habitação não encontrada. Lê o QR da tua habitação novamente.");
            }
            @Override public void onError(String message) { callback.onError(message); }
        });
    }

    /** Converts the API hierarchy into the home shown on screen. */
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
