package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Amenity;
import com.example.condosched.ui.adapter.AmenityAdapter;
import com.example.condosched.domain.DataError;
import com.example.condosched.viewmodel.AmenitiesViewModel;
import com.example.condosched.viewmodel.UiState;

import java.util.Collections;
import java.util.List;

/**
 * Displays state and forwards user actions. Network requests belong to the Repository.
 */
public class AmenitiesActivity extends SessionActivity {
    private TextView status, context, count;
    private View progress, retry;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (isFinishing()) return;
        setContentView(R.layout.activity_amenities);
        ScreenInsets.apply(this);
        status = findViewById(R.id.amenitiesStatus);
        context = findViewById(R.id.amenitiesContext);
        count = findViewById(R.id.amenitiesCount);
        progress = findViewById(R.id.amenitiesProgress);
        retry = findViewById(R.id.retryAmenitiesButton);
        list = findViewById(R.id.amenitiesList);
        context.setText(currentSession().getHome().condominium);
        BottomNavigation.bind(this, R.id.navCondo);
        findViewById(R.id.navCondo).setOnClickListener(v -> startActivity(
                new Intent(this, CondominiumActivity.class).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));
        list.setOnItemClickListener((parent, view, position, id) -> {
            Amenity selected = (Amenity) parent.getItemAtPosition(position);
            startActivity(new Intent(this, AmenityDetailActivity.class)
                    .putExtra(AmenityDetailActivity.EXTRA_AMENITY, selected));
        });

        AmenitiesViewModel viewModel = new ViewModelProvider(this).get(AmenitiesViewModel.class);
        viewModel.getState().observe(this, this::render);
        retry.setOnClickListener(v -> viewModel.retry());
        viewModel.load(currentSession().getHome());
    }

    private void render(UiState<List<Amenity>> state) {
        List<Amenity> amenities = state.getData() == null ? Collections.emptyList() : state.getData();
        boolean loading = state.getStatus() == UiState.Status.LOADING;
        boolean error = state.getStatus() == UiState.Status.ERROR;
        boolean success = state.getStatus() == UiState.Status.SUCCESS;
        boolean content = success && !amenities.isEmpty();
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        retry.setVisibility(error ? View.VISIBLE : View.GONE);
        status.setVisibility(content ? View.GONE : View.VISIBLE);
        list.setVisibility(content ? View.VISIBLE : View.GONE);
        count.setVisibility(success ? View.VISIBLE : View.GONE);
        int size = amenities.size();
        count.setText(getResources().getQuantityString(R.plurals.amenities_count, size, size));

        if (loading) status.setText(R.string.amenities_loading);
        else if (error) status.setText(errorMessage(state.getError()));
        else if (success && !content) status.setText(R.string.amenities_empty);
        else if (content) list.setAdapter(new AmenityAdapter(this, amenities));
    }

    private int errorMessage(DataError error) {
        if (error == DataError.HOME_UNAVAILABLE) return R.string.amenities_home_error;
        if (error == DataError.INVALID_RESPONSE) return R.string.amenities_data_error;
        return R.string.amenities_error;
    }
}
