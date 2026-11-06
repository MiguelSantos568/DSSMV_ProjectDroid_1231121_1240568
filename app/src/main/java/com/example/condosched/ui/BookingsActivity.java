package com.example.condosched.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Booking;
import com.example.condosched.ui.adapter.BookingAdapter;
import com.example.condosched.ui.display.BookingDisplay;
import com.example.condosched.viewmodel.BookingsViewModel;
import com.example.condosched.viewmodel.UiState;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class BookingsActivity extends SessionActivity {
    private BookingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        setContentView(R.layout.activity_bookings);
        ScreenInsets.apply(this);
        BottomNavigation.bind(this, R.id.navBookings);
        viewModel = new ViewModelProvider(this).get(BookingsViewModel.class);
        viewModel.getState().observe(this, this::render);
        viewModel.getDeletion().observe(this, result -> {
            render(viewModel.getState().getValue());
            if (result.getStatus() == UiState.Status.SUCCESS) {
                Toast.makeText(this, R.string.booking_deleted, Toast.LENGTH_SHORT).show();
                viewModel.consumeDeletionResult();
            } else if (result.getStatus() == UiState.Status.ERROR) {
                Toast.makeText(this, UiErrors.message(result.getError()), Toast.LENGTH_LONG).show();
                viewModel.consumeDeletionResult();
            }
        });
        findViewById(R.id.bookingsRefresh).setOnClickListener(v -> viewModel.load(currentSession().getHome().unitId));
    }

    private void render(UiState<List<Booking>> result) {
        boolean loading = result.getStatus() == UiState.Status.LOADING;
        boolean success = result.getStatus() == UiState.Status.SUCCESS;
        boolean empty = success && result.getData().isEmpty();
        boolean deleting = viewModel.isDeleting();
        findViewById(R.id.bookingsRefresh).setEnabled(!loading && !deleting);
        TextView status = findViewById(R.id.bookingsStatus);
        status.setVisibility(success && !empty && !deleting ? View.GONE : View.VISIBLE);
        if (deleting) status.setText(R.string.booking_deleting);
        else if (loading) status.setText(R.string.bookings_loading);
        else if (result.getStatus() == UiState.Status.ERROR) status.setText(UiErrors.message(result.getError()));
        else if (empty) status.setText(R.string.bookings_empty);
        ListView list = findViewById(R.id.bookingsList);
        list.setVisibility(success && !empty ? View.VISIBLE : View.GONE);
        if (success) list.setAdapter(new BookingAdapter(this, result.getData(), deleting, this::confirmDeletion));
    }

    private void confirmDeletion(Booking booking) {
        if (viewModel.isDeleting()) return;
        String name = booking.getAmenityName();
        if (name == null || name.trim().isEmpty()) name = getString(R.string.amenity_unnamed);
        String period = getString(R.string.booking_period,
                BookingDisplay.dateTime(this, booking.getStartTime()), BookingDisplay.dateTime(this, booking.getEndTime()));
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.booking_delete_title)
                .setMessage(getString(R.string.booking_delete_message, name, period))
                .setNegativeButton(R.string.booking_keep, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.booking_delete, (dialog, which) ->
                        viewModel.deleteBooking(currentSession().getHome().unitId, booking.getId()))
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null && !isFinishing()) viewModel.load(currentSession().getHome().unitId);
    }
}
