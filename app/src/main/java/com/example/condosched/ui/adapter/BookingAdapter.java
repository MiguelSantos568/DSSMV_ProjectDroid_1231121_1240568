package com.example.condosched.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.condosched.R;
import com.example.condosched.domain.Booking;
import com.example.condosched.ui.display.BookingDisplay;
import java.util.List;

public class BookingAdapter extends ArrayAdapter<Booking> {
    public BookingAdapter(Context context, List<Booking> bookings) {
        super(context, R.layout.item_booking, bookings);
    }

    @Override
    public View getView(int position, View recycled, ViewGroup parent) {
        View row = recycled == null ? LayoutInflater.from(getContext()).inflate(R.layout.item_booking, parent, false) : recycled;
        Booking booking = getItem(position);
        String name = booking.getAmenityName();
        if (name == null || name.trim().isEmpty()) name = getContext().getString(R.string.amenity_unnamed);
        ((TextView) row.findViewById(R.id.bookingRowTitle)).setText(getContext().getString(
                R.string.booking_reference, booking.getId(), name));
        ((TextView) row.findViewById(R.id.bookingRowPeriod)).setText(getContext().getString(R.string.booking_period,
                BookingDisplay.dateTime(getContext(), booking.getStartTime()), BookingDisplay.dateTime(getContext(), booking.getEndTime())));
        ((TextView) row.findViewById(R.id.bookingRowStatus)).setText(BookingDisplay.status(getContext(), booking.getStatus()));
        return row;
    }
}
