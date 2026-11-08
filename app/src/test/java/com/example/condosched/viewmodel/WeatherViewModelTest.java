package com.example.condosched.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.ViewModelStore;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Forecast;
import com.example.condosched.repository.RepositoryCallback;
import com.example.condosched.repository.WeatherRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class WeatherViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();

    @Test
    public void rotationKeepsPendingRequestAndSuccessfulForecastWithoutReloading() {
        FakeRepository repository = new FakeRepository();
        WeatherViewModel model = new WeatherViewModel(repository);
        model.load(null, null, 14, "pt");
        assertTrue(model.hasLoaded("pt", 14));
        assertEquals(UiState.Status.LOADING, model.getState().getValue().getStatus());
        model.load(null, null, 14, "pt");
        Forecast forecast = new Forecast(LocalDate.of(2026, 11, 8), 1000, "Sol");
        repository.pending.onSuccess(Collections.singletonList(forecast));
        model.load(null, null, 14, "pt");
        assertEquals(1, repository.loads);
        assertSame(forecast, model.getState().getValue().getData().get(0));
    }

    @Test
    public void languageChangeRequestsTranslatedForecastAndDaysAreLimited() {
        FakeRepository repository = new FakeRepository();
        WeatherViewModel model = new WeatherViewModel(repository);
        model.load(41.0, -8.0, 90, "en");
        assertEquals(14, repository.days);
        repository.pending.onSuccess(Collections.emptyList());
        assertFalse(model.hasLoaded("pt", 14));
        model.load(41.0, -8.0, 14, "pt");
        assertEquals(2, repository.loads);
        assertEquals("pt", repository.language);
    }

    @Test
    public void failedRequestExposesErrorAndAllowsAnotherAttempt() {
        FakeRepository repository = new FakeRepository();
        WeatherViewModel model = new WeatherViewModel(repository);
        model.load(null, null, 7, "en");
        repository.pending.onError(DataError.LOAD_FAILED);
        assertEquals(UiState.Status.ERROR, model.getState().getValue().getStatus());
        assertEquals(DataError.LOAD_FAILED, model.getState().getValue().getError());
        assertFalse(model.hasLoaded("en", 7));
        model.load(null, null, 7, "en");
        assertEquals(2, repository.loads);
    }

    @Test
    public void clearingViewModelCancelsWeatherRequest() {
        FakeRepository repository = new FakeRepository();
        WeatherViewModel model = new WeatherViewModel(repository);
        ViewModelStore store = new ViewModelStore();
        store.put("weather", model);
        model.load(null, null, 7, "en");
        store.clear();
        assertTrue(repository.cancelled);
    }

    private static class FakeRepository extends WeatherRepository {
        int loads;
        int days;
        String language;
        boolean cancelled;
        RepositoryCallback<List<Forecast>> pending;

        @Override
        public void load(Double latitude, Double longitude, int days, String language,
                         RepositoryCallback<List<Forecast>> callback) {
            loads++;
            this.days = days;
            this.language = language;
            pending = callback;
        }

        @Override
        public void cancel() { cancelled = true; }
    }
}
