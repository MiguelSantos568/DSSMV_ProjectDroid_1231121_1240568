package com.example.condosched.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;

import com.example.condosched.R;
import com.example.condosched.network.WeatherService;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WeatherActivity extends AppCompatActivity {

    public static final String DAYS = "days";
    private static final int DEFAULT_DAYS = 7;

    private ListView list;
    private ProgressBar progress;
    private TextView message;

    private int days;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private CancellationSignal locationCancel;
    private ActivityResultLauncher<String[]> locationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        ScreenInsets.apply(this);

        list = findViewById(R.id.weatherList);
        progress = findViewById(R.id.progress);
        message = findViewById(R.id.weatherMessage);

        days = getIntent().getIntExtra(DAYS, DEFAULT_DAYS);
        locationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> loadWeather());

        if (hasLocationPermission()) {
            loadWeather();
        } else {
            locationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }

    // ------------------------------------------------------------ location

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Gets the device location (if allowed), then fetches the forecast. Falls back to the default location. */
    @SuppressLint("MissingPermission") // guarded by hasLocationPermission()
    private void loadWeather() {
        if (!hasLocationPermission()) {
            fetchForecast(null);
            return;
        }
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (lm == null) {
            fetchForecast(null);
            return;
        }
        try {
            String provider = null;
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
            } else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                provider = LocationManager.GPS_PROVIDER;
            }
            if (provider == null) {
                fetchForecast(null);
                return;
            }
            Location last = lm.getLastKnownLocation(provider);
            if (last != null) {
                fetchForecast(last);
                return;
            }
            locationCancel = new CancellationSignal();
            LocationManagerCompat.getCurrentLocation(lm, provider, locationCancel,
                    ContextCompat.getMainExecutor(this), this::fetchForecast);
        } catch (SecurityException | IllegalArgumentException e) {
            fetchForecast(null);
        }
    }

    // ------------------------------------------------------------- network

    /** Calls WeatherService off the main thread. A null location uses the service default (Porto). */
    private void fetchForecast(Location location) {
        ioExecutor.execute(() -> {
            List<Row> rows = new ArrayList<>();
            String error = null;
            try {
                // WeatherService has a private constructor and instance methods,
                // so it is created reflectively until those are made static/public.
                Constructor<WeatherService> ctor = WeatherService.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                WeatherService service = ctor.newInstance();
                List<String> forecast = location == null
                        ? service.getWeather(this)
                        : service.getWeather(this,
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()));
                rows = toRows(forecast);
            } catch (Exception e) {
                error = "Não foi possível obter a previsão.\n" + e.getMessage();
            }
            List<Row> finalRows = rows;
            String finalError = error;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                showResult(finalRows, finalError);
            });
        });
    }

    /** Parses "yyyy-MM-dd;code;description" entries and keeps only the next {@code days} days. */
    private List<Row> toRows(List<String> forecast) {
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("EEEE, d MMM", locale);
        List<Row> rows = new ArrayList<>();
        for (String entry : forecast) {
            if (rows.size() >= days) break;
            String[] parts = entry.split(";", 3);
            if (parts.length < 3) continue;
            try {
                String date = LocalDate.parse(parts[0]).format(format);
                rows.add(new Row(date, iconFor(Integer.parseInt(parts[1])), parts[2]));
            } catch (RuntimeException ignored) {
                // skip malformed entry
            }
        }
        return rows;
    }

    private void showResult(List<Row> rows, String error) {
        progress.setVisibility(View.GONE);
        if (error != null || rows.isEmpty()) {
            message.setText(error != null ? error : "Não foi possível obter a previsão.");
            message.setVisibility(View.VISIBLE);
            return;
        }
        list.setAdapter(new ForecastAdapter(rows));
        list.setVisibility(View.VISIBLE);
    }

    // ------------------------------------------------------------- display

    private static final class Row {
        final String date;
        final int icon;
        final String description;

        Row(String date, int icon, String description) {
            this.date = date;
            this.icon = icon;
            this.description = description;
        }
    }

    private final class ForecastAdapter extends BaseAdapter {
        private final List<Row> rows;

        ForecastAdapter(List<Row> rows) {
            this.rows = Collections.unmodifiableList(rows);
        }

        @Override public int getCount() { return rows.size(); }
        @Override public Object getItem(int position) { return rows.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LinearLayout item = new LinearLayout(WeatherActivity.this);
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setGravity(Gravity.CENTER_VERTICAL);
                item.setPadding(0, dp(8), 0, dp(8));

                ImageView icon = new ImageView(WeatherActivity.this);
                icon.setLayoutParams(new LinearLayout.LayoutParams(dp(56), dp(56)));
                icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                item.addView(icon);

                LinearLayout texts = new LinearLayout(WeatherActivity.this);
                texts.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                textParams.setMarginStart(dp(16));
                texts.setLayoutParams(textParams);

                TextView date = new TextView(WeatherActivity.this);
                date.setTypeface(Typeface.DEFAULT_BOLD);
                date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                texts.addView(date);

                TextView description = new TextView(WeatherActivity.this);
                description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                texts.addView(description);

                item.addView(texts);
                holder = new ViewHolder(icon, date, description);
                item.setTag(holder);
                convertView = item;
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Row row = rows.get(position);
            holder.icon.setImageResource(row.icon);
            holder.date.setText(row.date);
            holder.description.setText(row.description);
            return convertView;
        }
    }

    private static final class ViewHolder {
        final ImageView icon;
        final TextView date;
        final TextView description;

        ViewHolder(ImageView icon, TextView date, TextView description) {
            this.icon = icon;
            this.date = date;
            this.description = description;
        }
    }

    /** Same code groups as WeatherService.toDescription, mapped to the drawables. */
    private static int iconFor(int code) {
        switch (code) {
            case 1000:
                return R.drawable.ic_sunny;
            case 1003:
                return R.drawable.ic_partly_cloudy;
            case 1006: case 1009:
                return R.drawable.ic_cloudy;
            case 1135: case 1147:
                return R.drawable.ic_fog;
            case 1063: case 1072: case 1150: case 1153: case 1168:
            case 1180: case 1183: case 1186: case 1189: case 1198:
            case 1204: case 1207: case 1240: case 1249: case 1252:
            case 1261: case 1264:
                return R.drawable.ic_rain;
            case 1171: case 1192: case 1195: case 1201: case 1237:
            case 1243: case 1246:
                return R.drawable.ic_heavy_rain;
            case 1066: case 1114: case 1117: case 1210: case 1213:
            case 1216: case 1219: case 1222: case 1225: case 1255:
            case 1258:
                return R.drawable.ic_snow;
            case 1087: case 1273: case 1276: case 1279: case 1282:
                return R.drawable.ic_thunder;
            default:
                return R.drawable.ic_weather_unknown;
        }
    }

    @Override
    protected void onDestroy() {
        if (locationCancel != null) locationCancel.cancel();
        ioExecutor.shutdownNow();
        super.onDestroy();
    }
}