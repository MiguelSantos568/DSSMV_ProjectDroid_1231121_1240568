package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.condosched.R;
import com.example.condosched.auth.LoginActivity;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.DTO.AmenityDTO;
import com.example.condosched.network.AmenityService;
import com.example.condosched.network.HabitationService;
import com.example.condosched.session.SessionManager;
import java.util.List;
import java.util.Map;

public class AmenitiesActivity extends AppCompatActivity {
    private SessionManager session;
    private TextView status, context;
    private View progress, retry;
    private ListView list;
    private final AmenityService service = new AmenityService();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        session = new SessionManager(this);
        if (!session.hasSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.amenitiesgrid);
        ScreenInsets.apply(this);
        status = findViewById(R.id.amenitiesStatus);
        context = findViewById(R.id.amenitiesContext);
        progress = findViewById(R.id.amenitiesProgress);
        retry = findViewById(R.id.retryAmenitiesButton);
        list = findViewById(R.id.amenitiesList);
        BottomNavigation.bind(this, R.id.navCondo);
        // Amenities is a child screen of Condominium; the tab also returns to its overview.
        findViewById(R.id.navCondo).setOnClickListener(v -> startActivity(
                new Intent(this, CondominiumActivity.class).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));
        retry.setOnClickListener(v -> load());
        load();
    }

    private void load() {
        findViewById(R.id.amenitiesCount).setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        retry.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        status.setVisibility(View.VISIBLE);
        status.setText(R.string.amenities_loading);
        Habitation home = session.getHabitation();
        showContext(home);
        if (home.condominiumId > 0) {
            loadCondominium(home);
        } else {
            // Sessions saved before condominium IDs were introduced remain valid.
            new HabitationService().find(home.unitId, new HabitationService.Callback() {
                @Override public void onSuccess(Habitation resolved) {
                    if (isFinishing() || isDestroyed()) return;
                    session.save(resolved, session.getName(), session.getEmail());
                    showContext(resolved);
                    loadCondominium(resolved);
                }
                @Override public void onError(String message) {
                    if (!isFinishing() && !isDestroyed()) showError();
                }
            });
        }
    }

    private void showContext(Habitation home) {
        context.setText(home.condominium);
    }

    private void loadCondominium(Habitation home) {
        service.getByCondominium(home.condominiumId, new AmenityService.AmenityCallback() {
            @Override public void onSuccess(List<AmenityDTO> amenities, Map<Long, String> buildingNames) {
                if (isFinishing() || isDestroyed()) return;
                progress.setVisibility(View.GONE);
                TextView count = findViewById(R.id.amenitiesCount);
                count.setText(getString(R.string.amenities_count, amenities.size()));
                count.setVisibility(View.VISIBLE);
                if (amenities.isEmpty()) {
                    status.setText(R.string.amenities_empty);
                } else {
                    status.setVisibility(View.GONE);
                    list.setAdapter(new AmenityAdapter(AmenitiesActivity.this, amenities, buildingNames));
                    list.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onError() {
                if (!isFinishing() && !isDestroyed()) showError();
            }
        });
    }

    private void showError() {
        progress.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        status.setText(R.string.amenities_error);
        status.setVisibility(View.VISIBLE);
        retry.setVisibility(View.VISIBLE);
    }

    @Override protected void onDestroy() {
        service.cancel();
        super.onDestroy();
    }
}
