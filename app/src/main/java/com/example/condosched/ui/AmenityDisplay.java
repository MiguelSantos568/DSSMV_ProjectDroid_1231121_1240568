package com.example.condosched.ui;

import android.content.Context;
import com.example.condosched.R;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Formatting shared by the list and the detail screen. */
final class AmenityDisplay {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
    private AmenityDisplay() {}

    static int icon(String type) {
        return "pool".equalsIgnoreCase(type) ? R.drawable.ic_pool
                : "gym".equalsIgnoreCase(type) ? R.drawable.ic_gym : R.drawable.ic_leaf;
    }

    static String time(Context context, String value) {
        if (value != null) {
            try { return LocalTime.parse(value).format(TIME); }
            catch (DateTimeParseException ignored) { }
        }
        return context.getString(R.string.amenity_time_unknown);
    }

    static String fee(Context context, Double value) {
        if (value == null || !Double.isFinite(value) || value < 0) {
            return context.getString(R.string.detail_not_provided);
        }
        if (value == 0) return context.getString(R.string.detail_free);
        NumberFormat number = NumberFormat.getNumberInstance(
                context.getResources().getConfiguration().getLocales().get(0));
        number.setMinimumFractionDigits(2);
        number.setMaximumFractionDigits(2);
        // TheCondo provides an amount but does not specify a currency.
        return context.getString(R.string.detail_fee_amount, number.format(value));
    }
}
