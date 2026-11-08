package com.example.condosched.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Forecast;
import com.example.condosched.repository.RepositoryCallback;
import com.example.condosched.repository.WeatherRepository;
import java.util.Collections;
import java.util.List;

public class WeatherViewModel extends ViewModel {
    public static final int MAX_DAYS = 14;
    private final WeatherRepository repository;
    private final MutableLiveData<UiState<List<Forecast>>> state = new MutableLiveData<>(UiState.idle());
    private String loadedLanguage;
    private int loadedDays;

    public WeatherViewModel() {
        this(new WeatherRepository());
    }

    WeatherViewModel(WeatherRepository repository) {
        this.repository = repository;
    }

    public LiveData<UiState<List<Forecast>>> getState() {
        return state;
    }

    public boolean hasLoaded(String language, int days) {
        return language.equals(loadedLanguage) && loadedDays == days
                && state.getValue().getStatus() != UiState.Status.IDLE
                && state.getValue().getStatus() != UiState.Status.ERROR;
    }

    public void load(Double latitude, Double longitude, int days, String language) {
        days = Math.max(1, Math.min(MAX_DAYS, days));
        if (hasLoaded(language, days)) return;
        loadedLanguage = language;
        loadedDays = days;
        state.setValue(UiState.loading(Collections.emptyList()));
        repository.load(latitude, longitude, days, language, new RepositoryCallback<List<Forecast>>() {
            @Override
            public void onSuccess(List<Forecast> forecasts) {
                state.setValue(UiState.success(forecasts));
            }

            @Override
            public void onError(DataError error) {
                state.setValue(UiState.failure(error, Collections.emptyList()));
            }
        });
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
