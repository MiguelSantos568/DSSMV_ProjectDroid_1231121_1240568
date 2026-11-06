package com.example.condosched.ui;

import com.example.condosched.R;
import com.example.condosched.domain.DataError;

/**
 * Os repositórios devolvem códigos; só a interface escolhe mensagens traduzidas.
 */
final class UiErrors {
    private UiErrors() {
    }

    static int message(DataError error) {
        if (error == DataError.INVALID_QR) return R.string.qr_invalid;
        if (error == DataError.INVALID_NAME) return R.string.name_required;
        if (error == DataError.INVALID_EMAIL) return R.string.email_invalid;
        if (error == DataError.HOME_UNAVAILABLE) return R.string.amenities_home_error;
        if (error == DataError.INVALID_RESPONSE) return R.string.data_error;
        if (error == DataError.SERVICE_UNAVAILABLE) return R.string.service_unavailable;
        if (error == DataError.INVALID_BOOKING_TIME) return R.string.booking_invalid_time;
        if (error == DataError.OUTSIDE_OPENING_HOURS) return R.string.booking_outside_hours;
        if (error == DataError.INVALID_GUEST_COUNT) return R.string.booking_invalid_people;
        if (error == DataError.RESIDENT_REQUIRED) return R.string.booking_resident_required;
        if (error == DataError.NO_RESIDENTS) return R.string.booking_no_residents;
        if (error == DataError.BOOKING_CONFLICT) return R.string.booking_conflict;
        if (error == DataError.AMENITY_UNAVAILABLE) return R.string.booking_unavailable;
        if (error == DataError.BOOKING_FAILED) return R.string.booking_failed;
        if (error == DataError.BOOKING_SERVICE_UNAVAILABLE) return R.string.booking_service_unavailable;
        return R.string.connection_error;
    }
}
