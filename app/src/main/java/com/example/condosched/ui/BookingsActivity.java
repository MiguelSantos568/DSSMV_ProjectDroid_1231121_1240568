package com.example.condosched.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.ui.adapter.BookingAdapter;
import com.example.condosched.viewmodel.BookingsViewModel;
import com.example.condosched.viewmodel.UiState;

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
        viewModel.getState().observe(this, result -> {
            boolean loading = result.getStatus() == UiState.Status.LOADING;
            boolean success = result.getStatus() == UiState.Status.SUCCESS;
            boolean empty = success && result.getData().isEmpty();
            findViewById(R.id.bookingsRefresh).setEnabled(!loading);
            TextView status = findViewById(R.id.bookingsStatus);
            status.setVisibility(success && !empty ? View.GONE : View.VISIBLE);
            if (loading) status.setText(R.string.bookings_loading);
            else if (result.getStatus() == UiState.Status.ERROR) status.setText(UiErrors.message(result.getError()));
            else if (empty) status.setText(R.string.bookings_empty);
            ListView list = findViewById(R.id.bookingsList);
            list.setVisibility(success && !empty ? View.VISIBLE : View.GONE);
            if (success) list.setAdapter(new BookingAdapter(this, result.getData()));
        });
        findViewById(R.id.bookingsRefresh).setOnClickListener(v -> viewModel.load(currentSession().getHome().unitId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null && !isFinishing()) viewModel.load(currentSession().getHome().unitId);
    }
}
