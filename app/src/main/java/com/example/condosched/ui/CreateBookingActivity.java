package com.example.condosched.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.Resident;
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
        observeViewModel();
        configureActions();
        viewModel.load(currentSession().getHome().unitId, amenity);
        restoreForm(state);
    }

    private void bindViews() {
        resident = findViewById(R.id.bookingResident);
        people = findViewById(R.id.bookingPeople);
        notes = findViewById(R.id.bookingNotes);
        status = findViewById(R.id.bookingStatus);
    }

    private void showAmenity(Amenity amenity) {
        ((TextView) findViewById(R.id.bookingAmenity)).setText(amenity.getName());
        ((TextView) findViewById(R.id.bookingHours)).setText(getString(R.string.amenity_hours,
                AmenityDisplay.time(this, amenity.getOpeningTime()), AmenityDisplay.time(this, amenity.getClosingTime())));
        ((TextView) findViewById(R.id.bookingCapacity)).setText(amenity.getCapacity() > 0
                ? getString(R.string.amenity_capacity, amenity.getCapacity()) : getString(R.string.amenity_capacity_unknown));
        ((TextView) findViewById(R.id.bookingFee)).setText(getString(R.string.booking_fee,
                AmenityDisplay.fee(this, amenity.getBookingFee())));
    }

    private void observeViewModel() {
        viewModel.getResidents().observe(this, this::renderResidents);
        viewModel.getResult().observe(this, result -> renderStatus());
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        viewModel.getDate().observe(this, date -> ((TextView) findViewById(R.id.bookingDate)).setText(
                getString(R.string.booking_date, date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)))));
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
