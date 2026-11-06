package com.example.condosched.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Parameters of the two SQL functions exposed by Supabase.
 */
public class ProfileRequestDTO {
    @SerializedName("p_unit_id")
    private long unitId;
    @SerializedName("p_full_name")
    private String fullName;
    @SerializedName("p_email")
    private String email;

    public ProfileRequestDTO() {
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
