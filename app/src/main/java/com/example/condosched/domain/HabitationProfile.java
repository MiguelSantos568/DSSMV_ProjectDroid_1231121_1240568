package com.example.condosched.domain;

public final class HabitationProfile {
    private final long unitId;
    private final String fullName, email;

    public HabitationProfile(long unitId, String fullName, String email) {
        this.unitId = unitId;
        this.fullName = fullName;
        this.email = email;
    }

    public long getUnitId() {
        return unitId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
