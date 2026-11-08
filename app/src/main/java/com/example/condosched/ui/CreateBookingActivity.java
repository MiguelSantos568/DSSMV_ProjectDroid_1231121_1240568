package com.example.condosched.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Location;
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
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.Resident;
import com.example.condosched.domain.Forecast;
import com.example.condosched.viewmodel.WeatherViewModel;
import com.example.condosched.ui.display.WeatherDisplay;
import com.example.condosched.viewmodel.CreateBookingViewModel;
import com.example.condosched.ui.display.AmenityDisplay;
import com.example.condosched.ui.display.BookingDisplay;
import com.example.condosched.viewmodel.UiState;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

    private WeatherViewModel weatherViewModel;
    private final WeatherLocation weatherLocation = new WeatherLocation();
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
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);
        if (state != null) restoredResidentId = state.getLong("selectedResident");
        locationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> loadWeather());
        weatherViewModel.getState().observe(this, result -> renderWeather());
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

    private String weatherLanguage() {
        return getResources().getConfiguration().getLocales().get(0).getLanguage();
    }

    private void startWeather() {
        if (weatherViewModel.hasLoaded(weatherLanguage(), WeatherViewModel.MAX_DAYS)) return;
        if (WeatherLocation.hasPermission(this)) loadWeather();
        else locationPermission.launch(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
    }

    private void loadWeather() {
        weatherLocation.request(this, this::fetchForecast);
    }

    private void fetchForecast(Location location) {
        weatherViewModel.load(location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(),
                WeatherViewModel.MAX_DAYS, weatherLanguage());
    }

    private void renderWeather() {
        if (weatherViewModel == null || viewModel == null) return;
        UiState<List<Forecast>> result = weatherViewModel.getState().getValue();
        weatherImage.setImageResource(R.drawable.ic_weather_unknown);
        if (result.getStatus() == UiState.Status.IDLE || result.getStatus() == UiState.Status.LOADING) {
            weatherText.setText(R.string.weather_loading);
            return;
        }
        LocalDate date = viewModel.getDate().getValue();
        if (result.getStatus() == UiState.Status.SUCCESS && date != null) {
            for (Forecast forecast : result.getData()) {
                if (date.equals(forecast.getDate())) {
                    weatherImage.setImageResource(WeatherDisplay.icon(forecast.getConditionCode()));
                    weatherText.setText(forecast.getDescription());
                    return;
                }
            }
        }
        weatherText.setText(R.string.weather_unavailable);
    }

    @Override
    protected void onDestroy() {
        weatherLocation.cancel();
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