package com.example.condosched.ui.display;

import android.content.Context;
import com.example.condosched.R;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class BookingDisplay {
    private BookingDisplay() { }

    public static String status(Context context, String status) {
        if (status == null || status.trim().isEmpty()) return context.getString(R.string.booking_status_unknown);
        switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "pending": return context.getString(R.string.booking_status_pending);
            case "confirmed": return context.getString(R.string.booking_status_confirmed);
            case "cancelled":
            case "canceled": return context.getString(R.string.booking_status_cancelled);
            case "rejected": return context.getString(R.string.booking_status_rejected);
            default: return status;
        }
    }

    public static String dateTime(Context context, String value) {
        try {
            return LocalDateTime.parse(value).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(context.getResources().getConfiguration().getLocales().get(0)));
        } catch (DateTimeParseException | NullPointerException error) {
            return context.getString(R.string.detail_not_provided);
        }
    }
}
