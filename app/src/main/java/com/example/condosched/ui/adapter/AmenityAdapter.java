package com.example.condosched.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import com.example.condosched.R;
import com.example.condosched.domain.Amenity;
import com.example.condosched.ui.display.AmenityDisplay;

import java.util.List;

/**
 * Associates one API amenity with one row of the ListView (Android adapters, T4).
 */
public final class AmenityAdapter extends ArrayAdapter<Amenity> {

    public AmenityAdapter(Context context, List<Amenity> amenities) {
        super(context, R.layout.item_amenity, amenities);

    }

    @Override
    public View getView(int position, View recycled, ViewGroup parent) {
        View row = recycled == null
                ? LayoutInflater.from(getContext()).inflate(R.layout.item_amenity, parent, false)
                : recycled;
        Amenity amenity = getItem(position);
        ((ImageView) row.findViewById(R.id.amenityIcon)).setImageResource(AmenityDisplay.icon(amenity.getType()));
        ((TextView) row.findViewById(R.id.amenityName)).setText(
                amenity.getName() == null || amenity.getName().trim().isEmpty()
                        ? getContext().getString(R.string.amenity_unnamed) : amenity.getName());
        String building = amenity.getBuildingName();
        ((TextView) row.findViewById(R.id.amenityBuilding)).setText(
                building == null || building.trim().isEmpty()
                        ? getContext().getString(R.string.amenity_building_fallback, amenity.getBuildingId())
                        : building);
        TextView description = row.findViewById(R.id.amenityDescription);
        String text = amenity.getDescription();
        description.setText(text);
        description.setVisibility(text == null || text.trim().isEmpty() ? View.GONE : View.VISIBLE);
        ((TextView) row.findViewById(R.id.amenityHours)).setText(getContext().getString(
                R.string.amenity_hours, AmenityDisplay.time(getContext(), amenity.getOpeningTime()),
                AmenityDisplay.time(getContext(), amenity.getClosingTime())));
        ((TextView) row.findViewById(R.id.amenityCapacity)).setText(amenity.getCapacity() > 0
                ? getContext().getResources().getQuantityString(R.plurals.amenity_capacity, amenity.getCapacity(), amenity.getCapacity())
                : getContext().getString(R.string.amenity_capacity_unknown));
        ((TextView) row.findViewById(R.id.amenityBooking)).setText(amenity.isRequiresBooking()
                ? R.string.needs_booking : R.string.free_access);
        return row;
    }

}
