package com.example.condosched.session;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.condosched.domain.Habitation;

public final class SessionManager {
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences("habitation_session", Context.MODE_PRIVATE);
    }

    public boolean hasSession() {
        return preferences.getBoolean("serverProfile", false)
                && preferences.getLong("unitId", 0) > 0
                && !getName().isEmpty() && !getEmail().isEmpty();
    }

    public void save(Habitation home, String name, String email) {
        preferences.edit().putBoolean("serverProfile", true).putLong("unitId", home.unitId)
                .putLong("condominiumId", home.condominiumId).putLong("buildingId", home.buildingId)
                .putString("condominium", home.condominium).putString("building", home.building)
                .putString("floor", home.floor).putString("unit", home.unit)
                .putString("name", name).putString("email", email).apply();
    }

    public Habitation getHabitation() {
        return new Habitation(preferences.getLong("unitId", 0),
                preferences.getLong("condominiumId", 0), preferences.getLong("buildingId", 0),
                preferences.getString("condominium", ""), preferences.getString("building", ""),
                preferences.getString("floor", ""), preferences.getString("unit", ""));
    }

    public String getName() {
        return preferences.getString("name", "");
    }

    public String getEmail() {
        return preferences.getString("email", "");
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
