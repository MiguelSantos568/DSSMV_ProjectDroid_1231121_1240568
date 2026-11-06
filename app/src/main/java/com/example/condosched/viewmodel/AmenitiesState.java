package com.example.condosched.viewmodel;

import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.DataError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One snapshot of everything the amenities screen needs to display.
 */
public final class AmenitiesState {
    public enum Status {LOADING, CONTENT, EMPTY, ERROR}

    private final Status status;
    private final Habitation home;
    private final List<Amenity> amenities;
    private final DataError error;

    private AmenitiesState(Status status, Habitation home, List<Amenity> amenities,
                           DataError error) {
        this.status = status;
        this.home = home;
        this.amenities = Collections.unmodifiableList(new ArrayList<>(amenities));
        this.error = error;
    }

    public static AmenitiesState loading(Habitation home) {
        return new AmenitiesState(Status.LOADING, home, Collections.emptyList(), null);
    }

    public static AmenitiesState success(Habitation home, List<Amenity> amenities) {
        return new AmenitiesState(amenities.isEmpty() ? Status.EMPTY : Status.CONTENT, home, amenities, null);
    }

    public static AmenitiesState failure(Habitation home, DataError error) {
        return new AmenitiesState(Status.ERROR, home, Collections.emptyList(), error);
    }

    public Status getStatus() {
        return status;
    }

    public Habitation getHome() {
        return home;
    }

    public List<Amenity> getAmenities() {
        return amenities;
    }

    public DataError getError() {
        return error;
    }
}
