package com.example.condosched.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HabitationProfile;
import com.example.condosched.domain.HomeSession;
import com.example.condosched.session.SessionManager;

/**
 * Fonte única da sessão local. Só esta classe comunica com o SessionManager.
 */
public final class SessionRepository {
    private static SessionRepository instance;
    private final SessionManager storage;
    private final MutableLiveData<HomeSession> session;

    public static synchronized SessionRepository getInstance(Context context) {
        if (instance == null) instance = new SessionRepository(context.getApplicationContext());
        return instance;
    }

    private SessionRepository(Context context) {
        storage = new SessionManager(context);
        HomeSession saved = storage.hasSession() ? new HomeSession(storage.getHabitation(),
                new HabitationProfile(storage.getHabitation().unitId, storage.getName(), storage.getEmail())) : null;
        session = new MutableLiveData<>(saved);
    }

    public LiveData<HomeSession> getSession() {
        return session;
    }

    public HomeSession current() {
        return session.getValue();
    }

    public void save(Habitation home, HabitationProfile profile) {
        storage.save(home, profile.getFullName(), profile.getEmail());
        session.setValue(new HomeSession(home, profile));
    }

    public void clear() {
        storage.clear();
        session.setValue(null);
    }
}
