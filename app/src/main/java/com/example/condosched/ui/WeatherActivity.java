package com.example.condosched.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.condosched.R;
import com.example.condosched.network.WeatherService;

import java.util.List;

public class WeatherActivity extends AppCompatActivity {

    public static final String DAYS = "days";
    private static final int DEFAULT_DAYS = 7;

    TextView weatherText;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);

        weatherText = findViewById(R.id.weatherText);
        progress = findViewById(R.id.progress);

        int days = getIntent().getIntExtra(DAYS, DEFAULT_DAYS);


        new Thread(() -> {
            String result;
            try {
                List<String> forecast = WeatherService.getWeather(days);
                result = String.join("\n\n", forecast);
            } catch (Exception e) {
                result = "Não foi possível obter a previsão.\n" + e.getMessage();
            }
            final String text = result;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                progress.setVisibility(View.GONE);
                weatherText.setText(text);
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        weatherText.setText("");
        weatherText = null;
        progress = null;
        super.onDestroy();
    }
}