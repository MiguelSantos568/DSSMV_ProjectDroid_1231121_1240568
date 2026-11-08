package com.example.condosched.ui;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Forecast;
import com.example.condosched.ui.adapter.ForecastAdapter;
import com.example.condosched.viewmodel.UiState;
import com.example.condosched.viewmodel.WeatherViewModel;
import java.util.List;

public final class WeatherActivity extends AppCompatActivity {
    public static final String DAYS = "days";
    private WeatherViewModel viewModel;
    private int days;
    private final WeatherLocation weatherLocation = new WeatherLocation();
    private final ActivityResultLauncher<String[]> locationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> loadWeather());

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_weather);
        ScreenInsets.apply(this);
        days = Math.max(1, Math.min(WeatherViewModel.MAX_DAYS, getIntent().getIntExtra(DAYS, 7)));
        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);
        viewModel.getState().observe(this, this::render);
        if (viewModel.hasLoaded(language(), days)) return;
        if (WeatherLocation.hasPermission(this)) loadWeather();
        else locationPermission.launch(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
    }

    private String language() {
        return getResources().getConfiguration().getLocales().get(0).getLanguage();
    }

    private void loadWeather() {
        weatherLocation.request(this, this::fetchForecast);
    }

    private void fetchForecast(Location location) {
        viewModel.load(location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(), days, language());
    }

    private void render(UiState<List<Forecast>> state) {
        boolean loading = state.getStatus() == UiState.Status.IDLE || state.getStatus() == UiState.Status.LOADING;
        boolean content = state.getStatus() == UiState.Status.SUCCESS && !state.getData().isEmpty();
        findViewById(R.id.progress).setVisibility(loading ? View.VISIBLE : View.GONE);
        TextView message = findViewById(R.id.weatherMessage);
        message.setVisibility(!loading && !content ? View.VISIBLE : View.GONE);
        message.setText(R.string.weather_unavailable);
        ListView list = findViewById(R.id.weatherList);
        list.setVisibility(content ? View.VISIBLE : View.GONE);
        if (content) list.setAdapter(new ForecastAdapter(this, state.getData()));
    }

    @Override
    protected void onDestroy() {
        weatherLocation.cancel();
        super.onDestroy();
    }
}
