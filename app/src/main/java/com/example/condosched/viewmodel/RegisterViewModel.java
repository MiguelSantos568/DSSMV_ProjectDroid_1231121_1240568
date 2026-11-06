package com.example.condosched.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HabitationProfile;
import com.example.condosched.domain.ProfileContactValidator;
import com.example.condosched.repository.HabitationProfileRepository;
import com.example.condosched.repository.SessionRepository;
import com.example.condosched.repository.RepositoryCallback;

public class RegisterViewModel extends AndroidViewModel {
    private final HabitationProfileRepository profiles = new HabitationProfileRepository();
    private final SessionRepository sessions;
    private final MutableLiveData<UiState<HabitationProfile>> state = new MutableLiveData<>(UiState.idle());

    public RegisterViewModel(Application application) {
        super(application);
        sessions = SessionRepository.getInstance(application);
    }

    public LiveData<UiState<HabitationProfile>> getState() {
        return state;
    }

    public void submit(Habitation home, String name, String email) {
        if (state.getValue().getStatus() == UiState.Status.LOADING) return;
        String fullName = name.trim();
        String address = email.trim();
        DataError invalid = ProfileContactValidator.validate(fullName, address);
        if (invalid != null) {
            state.setValue(UiState.failure(invalid, null));
            return;
        }
        state.setValue(UiState.loading(null));
        profiles.create(home.unitId, fullName, address, new RepositoryCallback<HabitationProfile>() {
            @Override
            public void onSuccess(HabitationProfile profile) {
                // O servidor pode devolver o perfil criado primeiro noutro dispositivo.
                sessions.save(home, profile);
                state.setValue(UiState.success(profile));
            }

            @Override
            public void onError(DataError error) {
                state.setValue(UiState.failure(error, null));
            }
        });
    }

    @Override
    protected void onCleared() {
        profiles.cancel();
    }
}
