package com.example.condosched.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HabitationProfile;
import com.example.condosched.domain.HabitationQr;
import com.example.condosched.repository.HabitationRepository;
import com.example.condosched.repository.HabitationProfileRepository;
import com.example.condosched.repository.SessionRepository;
import com.example.condosched.repository.RepositoryCallback;

public class AccessViewModel extends AndroidViewModel {
    public static final class Result {
        private final Habitation home;
        private final HabitationProfile profile;

        Result(Habitation home, HabitationProfile profile) {
            this.home = home;
            this.profile = profile;
        }

        public Habitation getHome() {
            return home;
        }

        public boolean needsRegistration() {
            return profile == null;
        }
    }

    private final HabitationRepository homes = new HabitationRepository();
    private final HabitationProfileRepository profiles = new HabitationProfileRepository();
    private final SessionRepository sessions;
    private final MutableLiveData<UiState<Result>> state = new MutableLiveData<>(UiState.idle());

    public AccessViewModel(Application application) {
        super(application);
        sessions = SessionRepository.getInstance(application);
    }

    public LiveData<UiState<Result>> getState() {
        return state;
    }

    public void scan(String contents) {
        if (state.getValue().getStatus() == UiState.Status.LOADING) return;
        final long unitId;
        try {
            unitId = HabitationQr.parse(contents);
        } catch (IllegalArgumentException error) {
            state.setValue(UiState.failure(DataError.INVALID_QR, null));
            return;
        }
        state.setValue(UiState.loading(null));
        homes.find(unitId, new RepositoryCallback<Habitation>() {
            @Override
            public void onSuccess(Habitation home) {
                profiles.find(home.unitId, new RepositoryCallback<HabitationProfile>() {
                    @Override
                    public void onSuccess(HabitationProfile profile) {
                        if (profile != null) sessions.save(home, profile);
                        state.setValue(UiState.success(new Result(home, profile)));
                    }

                    @Override
                    public void onError(DataError error) {
                        state.setValue(UiState.failure(error, null));
                    }
                });
            }

            @Override
            public void onError(DataError error) {
                state.setValue(UiState.failure(error, null));
            }
        });
    }

    // Depois de navegar, não repetir a navegação ao recriar a Activity.
    public void consumeResult() {
        state.setValue(UiState.idle());
    }

    @Override
    protected void onCleared() {
        homes.cancel();
        profiles.cancel();
    }
}