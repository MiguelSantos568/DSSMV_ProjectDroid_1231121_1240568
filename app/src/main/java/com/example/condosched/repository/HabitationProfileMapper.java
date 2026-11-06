package com.example.condosched.repository;

import com.example.condosched.domain.HabitationProfile;
import com.example.condosched.dto.HabitationProfileDTO;

public final class HabitationProfileMapper {
    private HabitationProfileMapper() {
    }

    public static HabitationProfile toDomain(HabitationProfileDTO dto) {
        return new HabitationProfile(dto.getUnitId(), dto.getFullName(), dto.getEmail());
    }
}