package com.example.condosched.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.Resident;
import com.example.condosched.network.WeatherService;
import com.example.condosched.viewmodel.CreateBookingViewModel;
import com.example.condosched.ui.display.AmenityDisplay;
import com.example.condosched.ui.display.BookingDisplay;
import com.example.condosched.viewmodel.UiState;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateBookingActivity extends SessionActivity {
    public static final String EXTRA_AMENITY = "amenity";
    private CreateBookingViewModel viewModel;
    private Spinner resident;
    private EditText people, notes;
    private TextView status;
    private long restoredResidentId;
    private List<Resident> displayedResidents = Collections.emptyList();
    private ImageView weatherImage;
    private TextView weatherText;

    // Weather state: each entry is "yyyy-MM-dd;code;description"
    private List<String> forecast = Collections.emptyList();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private CancellationSignal locationCancel;
    private ActivityResultLauncher<String[]> locationPermission;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        Object extra = getIntent().getSerializableExtra(EXTRA_AMENITY);
        if (!(extra instanceof Amenity) || ((Amenity) extra).getId() <= 0) {
            finish();
            return;
        }
        Amenity amenity = (Amenity) extra;
        setContentView(R.layout.activity_create_booking);
        ScreenInsets.apply(this);
        bindViews();
        showAmenity(amenity);
        viewModel = new ViewModelProvider(this).get(CreateBookingViewModel.class);
        if (state != null) restoredResidentId = state.getLong("selectedResident");
        locationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> loadWeather());
        observeViewModel();
        configureActions();
        viewModel.load(currentSession().getHome().unitId, amenity);
        restoreForm(state);
        startWeather();
    }

    private void bindViews() {
        resident = findViewById(R.id.bookingResident);
        people = findViewById(R.id.bookingPeople);
        notes = findViewById(R.id.bookingNotes);
        status = findViewById(R.id.bookingStatus);
        weatherImage = findViewById(R.id.bookingWeatherImage);
        weatherText = findViewById(R.id.bookingWeatherText);
    }

    private void showAmenity(Amenity amenity) {
        ((TextView) findViewById(R.id.bookingAmenity)).setText(amenity.getName());
        ((TextView) findViewById(R.id.bookingHours)).setText(getString(R.string.amenity_hours,
                AmenityDisplay.time(this, amenity.getOpeningTime()), AmenityDisplay.time(this, amenity.getClosingTime())));
        ((TextView) findViewById(R.id.bookingCapacity)).setText(amenity.getCapacity() > 0
                ? getResources().getQuantityString(R.plurals.amenity_capacity, amenity.getCapacity(), amenity.getCapacity())
                : getString(R.string.amenity_capacity_unknown));
        ((TextView) findViewById(R.id.bookingFee)).setText(getString(R.string.booking_fee,
                AmenityDisplay.fee(this, amenity.getBookingFee())));
    }

    private void observeViewModel() {
        viewModel.getResidents().observe(this, this::renderResidents);
        viewModel.getResult().observe(this, result -> renderStatus());
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        viewModel.getDate().observe(this, date -> {
            ((TextView) findViewById(R.id.bookingDate)).setText(
                    getString(R.string.booking_date, date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))));
            renderWeather();
        });
        viewModel.getStart().observe(this, time -> showTime(R.id.bookingStart, R.string.booking_start, time));
        viewModel.getEnd().observe(this, time -> showTime(R.id.bookingEnd, R.string.booking_end, time));
    }

    private void configureActions() {
        findViewById(R.id.bookingBack).setOnClickListener(v -> finish());
        findViewById(R.id.bookingDate).setOnClickListener(v -> pickDate());
        findViewById(R.id.bookingStart).setOnClickListener(v -> pickTime(true));
        findViewById(R.id.bookingEnd).setOnClickListener(v -> pickTime(false));
        findViewById(R.id.bookingRetry).setOnClickListener(v -> viewModel.retryResidents());
        findViewById(R.id.bookingSubmit).setOnClickListener(v -> viewModel.submit(selectedResidentId(),
                people.getText().toString(), notes.getText().toString()));
        findViewById(R.id.bookingViewList).setOnClickListener(v -> {
            startActivity(new Intent(this, BookingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
    }

    private void restoreForm(Bundle state) {
        if (state != null && state.containsKey("bookingDate")) {
            viewModel.setDate(LocalDate.parse(state.getString("bookingDate")));
            viewModel.setStart(LocalTime.parse(state.getString("bookingStart")));
            viewModel.setEnd(LocalTime.parse(state.getString("bookingEnd")));
        }
    }

    private void renderResidents(UiState<List<Resident>> state) {
        long selected = selectedResidentId();
        if (selected == 0) selected = restoredResidentId;
        displayedResidents = state.getData() == null ? Collections.emptyList() : state.getData();
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.booking_resident_hint));
        int selection = 0;
        for (Resident person : displayedResidents) {
            String name = ((person.getFirstName() == null ? "" : person.getFirstName()) + " "
                    + (person.getLastName() == null ? "" : person.getLastName())).trim();
            names.add(name.isEmpty() ? getString(R.string.booking_resident_fallback, person.getId()) : name);
            if (person.getId() == selected) selection = names.size() - 1;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resident.setAdapter(adapter);
        resident.setSelection(selection);
        renderStatus();
    }

    private long selectedResidentId() {
        if (viewModel == null || resident == null) return 0;
        int position = resident.getSelectedItemPosition() - 1;
        if (position < 0 || position >= displayedResidents.size()) return 0;
        return displayedResidents.get(position).getId();
    }

    private void renderStatus() {
        UiState<List<Resident>> residents = viewModel.getResidents().getValue();
        UiState<Booking> result = viewModel.getResult().getValue();
        boolean saving = result.getStatus() == UiState.Status.LOADING;
        boolean success = result.getStatus() == UiState.Status.SUCCESS;
        boolean ready = residents.getStatus() == UiState.Status.SUCCESS && !saving && !success;
        int[] controls = {R.id.bookingResident, R.id.bookingDate, R.id.bookingStart, R.id.bookingEnd,
                R.id.bookingPeople, R.id.bookingNotes, R.id.bookingSubmit};
        for (int id : controls) findViewById(id).setEnabled(ready);
        findViewById(R.id.bookingBack).setEnabled(!saving);
        findViewById(R.id.bookingViewList).setEnabled(!saving);
        findViewById(R.id.bookingSubmit).setVisibility(success ? View.GONE : View.VISIBLE);
        findViewById(R.id.bookingRetry).setVisibility(residents.getStatus() == UiState.Status.ERROR ? View.VISIBLE : View.GONE);
        if (saving) status.setText(R.string.booking_saving);
        else if (success) status.setText(getString(R.string.booking_success, result.getData().getId(),
                BookingDisplay.status(this, result.getData().getStatus())));
        else if (residents.getStatus() == UiState.Status.LOADING) status.setText(R.string.booking_loading);
        else if (residents.getStatus() == UiState.Status.ERROR) status.setText(UiErrors.message(residents.getError()));
        else if (result.getStatus() == UiState.Status.ERROR) status.setText(UiErrors.message(result.getError()));
        else status.setText("");
    }

    private void showTime(int id, int label, LocalTime time) {
        ((TextView) findViewById(id)).setText(getString(label, AmenityDisplay.time(this, time)));
    }

    private void pickDate() {
        LocalDate date = viewModel.getDate().getValue();
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, day) ->
                viewModel.setDate(LocalDate.of(year, month + 1, day)), date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        picker.show();
    }

    private void pickTime(boolean start) {
        LocalTime time = start ? viewModel.getStart().getValue() : viewModel.getEnd().getValue();
        new TimePickerDialog(this, (view, hour, minute) -> {
            if (start) viewModel.setStart(LocalTime.of(hour, minute));
            else viewModel.setEnd(LocalTime.of(hour, minute));
        }, time.getHour(), time.getMinute(), DateFormat.is24HourFormat(this)).show();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startWeather() {
        if (hasLocationPermission()) {
            loadWeather();
        } else {
            locationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    /** Gets the device location (if allowed) and then fetches the forecast. Falls back to the default location. */
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
        String provider = null;
        try {
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

    /** Calls WeatherService off the main thread. A null location uses the service's default (Porto). */
    private void fetchForecast(Location location) {
        ioExecutor.execute(() -> {
            List<String> result;
            try {
                // WeatherService has a private constructor and instance methods, so it is
                // instantiated reflectively (only this activity may be changed).
                Constructor<WeatherService> ctor = WeatherService.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                WeatherService service = ctor.newInstance();
                result = location == null
                        ? service.getWeather(this)
                        : service.getWeather(this,
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()));
            } catch (Exception e) {
                result = Collections.emptyList();
            }
            List<String> finalResult = result;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                forecast = finalResult;
                renderWeather();
            });
        });
    }

    /** Shows the forecast entry that matches the selected booking date. */
    private void renderWeather() {
        if (weatherImage == null || viewModel == null) return;
        LocalDate date = viewModel.getDate().getValue();
        if (date != null) {
            String prefix = date.toString() + ";";
            for (String entry : forecast) {
                if (!entry.startsWith(prefix)) continue;
                String[] parts = entry.split(";", 3);
                if (parts.length < 3) break;
                try {
                    renderWeatherImage(Integer.parseInt(parts[1]));
                    renderWeatherText(parts[2]);
                    return;
                } catch (NumberFormatException ignored) {
                    break;
                }
            }
        }
        // No forecast for this date (e.g. beyond 14 days) or request failed
        weatherImage.setImageResource(R.drawable.ic_weather_unknown);
        weatherText.setText(R.string.weather_unavailable);
    }

    private void renderWeatherImage(int code) {
        weatherImage.setImageResource(iconFor(code));
    }

    private void renderWeatherText(String description) {
        weatherText.setText(description);
    }

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

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putLong("selectedResident", selectedResidentId());
        if (viewModel != null && viewModel.getDate().getValue() != null) {
            state.putString("bookingDate", viewModel.getDate().getValue().toString());
            state.putString("bookingStart", viewModel.getStart().getValue().toString());
            state.putString("bookingEnd", viewModel.getEnd().getValue().toString());
        }
        super.onSaveInstanceState(state);
    }
}