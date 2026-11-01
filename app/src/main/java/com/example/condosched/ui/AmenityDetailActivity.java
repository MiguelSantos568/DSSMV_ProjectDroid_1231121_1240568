package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.condosched.R;
import com.example.condosched.auth.LoginActivity;
import com.example.condosched.domain.DTO.AmenityDTO;
import com.example.condosched.session.SessionManager;

public class AmenityDetailActivity extends AppCompatActivity {
    public static final String EXTRA_AMENITY = "amenity";
    public static final String EXTRA_BUILDING = "building_name";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        SessionManager session = new SessionManager(this);
        if (!session.hasSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        Object extra = getIntent().getSerializableExtra(EXTRA_AMENITY);
        if (!(extra instanceof AmenityDTO) || ((AmenityDTO) extra).getId() <= 0) {
            startActivity(new Intent(this, AmenitiesActivity.class));
            finish();
            return;
        }
        AmenityDTO amenity = (AmenityDTO) extra;
        setContentView(R.layout.activity_amenity_detail);
        ScreenInsets.apply(this);
        findViewById(R.id.detailBack).setOnClickListener(v -> finish());
        ((ImageView) findViewById(R.id.detailIcon)).setImageResource(AmenityDisplay.icon(amenity.getType()));
        text(R.id.detailName, fallback(amenity.getName(), R.string.amenity_unnamed));
        String building = getIntent().getStringExtra(EXTRA_BUILDING);
        text(R.id.detailLocation, session.getHabitation().condominium + " · "
                + (building == null || building.trim().isEmpty()
                ? getString(R.string.amenity_building_fallback, amenity.getBuildingId()) : building));
        text(R.id.detailDescription, fallback(amenity.getDescription(), R.string.detail_description_missing));
        text(R.id.detailHours, getString(R.string.amenity_hours,
                AmenityDisplay.time(this, amenity.getOpeningTime()),
                AmenityDisplay.time(this, amenity.getClosingTime())));
        text(R.id.detailCapacity, amenity.getCapacity() > 0
                ? getString(R.string.amenity_capacity, amenity.getCapacity())
                : getString(R.string.amenity_capacity_unknown));
        text(R.id.detailBooking, getString(amenity.isRequiresBooking()
                ? R.string.needs_booking : R.string.free_access));
        text(R.id.detailFee, AmenityDisplay.fee(this, amenity.getBookingFee()));
        text(R.id.detailRules, fallback(amenity.getRules(), R.string.detail_rules_missing));
    }

    private String fallback(String value, int resource) {
        return value == null || value.trim().isEmpty() ? getString(resource) : value;
    }

    private void text(int id, String value) { ((TextView) findViewById(id)).setText(value); }
}
