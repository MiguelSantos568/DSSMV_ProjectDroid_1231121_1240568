package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.condosched.R;
import com.example.condosched.domain.Amenity;
import com.example.condosched.ui.display.AmenityDisplay;

public class AmenityDetailActivity extends SessionActivity {
    public static final String EXTRA_AMENITY = "amenity";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        Object extra = getIntent().getSerializableExtra(EXTRA_AMENITY);
        if (!(extra instanceof Amenity) || ((Amenity) extra).getId() <= 0) {
            startActivity(new Intent(this, AmenitiesActivity.class));
            finish();
            return;
        }
        Amenity amenity = (Amenity) extra;
        setContentView(R.layout.activity_amenity_detail);
        ScreenInsets.apply(this);
        findViewById(R.id.detailBack).setOnClickListener(v -> finish());
        ((ImageView) findViewById(R.id.detailIcon)).setImageResource(AmenityDisplay.icon(amenity.getType()));
        text(R.id.detailName, fallback(amenity.getName(), R.string.amenity_unnamed));
        String building = amenity.getBuildingName();
        text(R.id.detailLocation, currentSession().getHome().condominium + " · "
                + (building == null || building.trim().isEmpty()
                ? getString(R.string.amenity_building_fallback, amenity.getBuildingId()) : building));
        text(R.id.detailDescription, fallback(amenity.getDescription(), R.string.detail_description_missing));
        text(R.id.detailHours, getString(R.string.amenity_hours,
                AmenityDisplay.time(this, amenity.getOpeningTime()),
                AmenityDisplay.time(this, amenity.getClosingTime())));
        text(R.id.detailCapacity, amenity.getCapacity() > 0
                ? getResources().getQuantityString(R.plurals.amenity_capacity, amenity.getCapacity(), amenity.getCapacity())
                : getString(R.string.amenity_capacity_unknown));
        text(R.id.detailBooking, getString(amenity.isRequiresBooking()
                ? R.string.needs_booking : R.string.free_access));
        text(R.id.detailFee, AmenityDisplay.fee(this, amenity.getBookingFee()));
        text(R.id.detailRules, fallback(amenity.getRules(), R.string.detail_rules_missing));
        findViewById(R.id.detailBook).setEnabled(amenity.isActive());
        findViewById(R.id.detailBook).setOnClickListener(v -> startActivity(new Intent(this, CreateBookingActivity.class)
                .putExtra(CreateBookingActivity.EXTRA_AMENITY, amenity)));
    }

    private String fallback(String value, int resource) {
        return value == null || value.trim().isEmpty() ? getString(resource) : value;
    }

    private void text(int id, String value) {
        ((TextView) findViewById(id)).setText(value);
    }
}
