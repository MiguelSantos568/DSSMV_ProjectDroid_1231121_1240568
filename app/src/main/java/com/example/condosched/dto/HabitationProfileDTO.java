package com.example.condosched.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The shared contact details for one home, as returned by Supabase.
 */
public class HabitationProfileDTO {
    @SerializedName("unit_id")
    private long unitId;
    @SerializedName("full_name")
    private String fullName;
    private String email;

    public HabitationProfileDTO() {
    }

    public long getUnitId() {
        return unitId;
    }

    public void setUnitId(long unitId) {
        this.unitId = unitId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
