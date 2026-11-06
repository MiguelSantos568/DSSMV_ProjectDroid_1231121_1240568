package com.example.condosched.domain;

/**
 * Casa selecionada e contacto partilhado guardados neste dispositivo.
 */
public final class HomeSession {
    private final Habitation home;
    private final HabitationProfile profile;

    public HomeSession(Habitation home, HabitationProfile profile) {
        this.home = home;
        this.profile = profile;
    }

    public Habitation getHome() {
        return home;
    }

    public HabitationProfile getProfile() {
        return profile;
    }
}
