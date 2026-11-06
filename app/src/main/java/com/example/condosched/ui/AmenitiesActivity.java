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
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.DataError;
import com.example.condosched.viewmodel.AmenitiesState;
import com.example.condosched.viewmodel.AmenitiesViewModel;

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

    private void render(AmenitiesState state) {
        Habitation home = state.getHome();
        context.setText(home.condominium);
        // Preserve support for sessions created before condominium IDs were stored locally.
        if (currentSession().getHome().condominiumId == 0 && home.condominiumId > 0) {
            sessionViewModel.updateHome(home);
        }
        boolean loading = state.getStatus() == AmenitiesState.Status.LOADING;
        boolean error = state.getStatus() == AmenitiesState.Status.ERROR;
        boolean content = state.getStatus() == AmenitiesState.Status.CONTENT;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        retry.setVisibility(error ? View.VISIBLE : View.GONE);
        status.setVisibility(content ? View.GONE : View.VISIBLE);
        list.setVisibility(content ? View.VISIBLE : View.GONE);
        count.setVisibility(loading || error ? View.GONE : View.VISIBLE);
        int size = state.getAmenities().size();
        count.setText(getResources().getQuantityString(R.plurals.amenities_count, size, size));

        if (loading) status.setText(R.string.amenities_loading);
        else if (error) status.setText(errorMessage(state.getError()));
        else if (!content) status.setText(R.string.amenities_empty);
        else list.setAdapter(new AmenityAdapter(this, state.getAmenities()));
    }

    private int errorMessage(DataError error) {
        if (error == DataError.HOME_UNAVAILABLE) return R.string.amenities_home_error;
        if (error == DataError.INVALID_RESPONSE) return R.string.amenities_data_error;
        return R.string.amenities_error;
    }
}
