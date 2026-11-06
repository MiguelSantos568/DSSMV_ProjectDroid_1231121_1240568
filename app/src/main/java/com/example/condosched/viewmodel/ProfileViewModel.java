package com.example.condosched.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.HomeSession;
import com.example.condosched.domain.HabitationProfile;
import com.example.condosched.repository.HabitationProfileRepository;
import com.example.condosched.repository.SessionRepository;
import com.example.condosched.repository.RepositoryCallback;

public class ProfileViewModel extends AndroidViewModel {
    private final HabitationProfileRepository profiles = new HabitationProfileRepository();
    private final SessionRepository sessions;
    private final MutableLiveData<UiState<HabitationProfile>> state = new MutableLiveData<>(UiState.idle());
    private boolean loaded;

    public ProfileViewModel(Application application) {
        super(application);
        sessions = SessionRepository.getInstance(application);
    }

    public LiveData<UiState<HabitationProfile>> getState() {
        return state;
    }

    public void load() {
        HomeSession session = sessions.current();
        if (loaded || session == null) return;
        loaded = true;
        state.setValue(UiState.loading(session.getProfile()));
        profiles.find(session.getHome().unitId, new RepositoryCallback<HabitationProfile>() {
            @Override
            public void onSuccess(HabitationProfile profile) {
                HomeSession current = sessions.current();
                // Uma resposta atrasada nunca volta a abrir uma sessão que já terminou.
                if (current == null || current.getHome().unitId != session.getHome().unitId) return;
                if (profile == null) state.setValue(UiState.failure(DataError.NOT_FOUND, session.getProfile()));
                else {
                    sessions.save(current.getHome(), profile);
                    state.setValue(UiState.success(profile));
                }
            }

            @Override
            public void onError(DataError error) {
                state.setValue(UiState.failure(error, session.getProfile()));
            }
        });
    }

    public void consumeMessage() {
        state.setValue(UiState.success(sessions.current() == null ? null : sessions.current().getProfile()));
    }

    @Override
    protected void onCleared() {
        profiles.cancel();
    }
}