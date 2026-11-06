package com.example.condosched.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.DataError;
import com.example.condosched.repository.BookingRepository;
import com.example.condosched.repository.RepositoryCallback;
import java.util.ArrayList;
import java.util.List;

public class BookingsViewModel extends ViewModel {
    private final BookingRepository repository;
    private final MutableLiveData<UiState<List<Booking>>> state = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Booking>> cancellation = new MutableLiveData<>(UiState.idle());

    public BookingsViewModel() {
        this(new BookingRepository());
    }

    BookingsViewModel(BookingRepository repository) {
        this.repository = repository;
    }

    public LiveData<UiState<List<Booking>>> getState() {
        return state;
    }

    public LiveData<UiState<Booking>> getCancellation() {
        return cancellation;
    }

    public boolean isCancelling() {
        return cancellation.getValue().getStatus() == UiState.Status.LOADING;
    }

    public void load(long unitId) {
        if (state.getValue().getStatus() == UiState.Status.LOADING || isCancelling()) return;
        state.setValue(UiState.loading(null));
        repository.loadForHome(unitId, new RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                state.setValue(UiState.success(bookings));
            }

            @Override
            public void onError(DataError error) {
                state.setValue(UiState.failure(error, null));
            }
        });
    }

    public void cancelBooking(long unitId, long bookingId) {
        if (state.getValue().getStatus() != UiState.Status.SUCCESS || isCancelling()) return;
        List<Booking> bookings = state.getValue().getData();
        Booking selected = null;
        for (Booking booking : bookings) {
            if (booking.getId() == bookingId && booking.getUnitId() == unitId) selected = booking;
        }
        if (selected == null || !selected.canCancel()) return;
        String amenityName = selected.getAmenityName();
        cancellation.setValue(UiState.loading(selected));
        repository.cancelBooking(unitId, bookingId, new RepositoryCallback<Booking>() {
            @Override
            public void onSuccess(Booking cancelled) {
                cancelled.setAmenityName(amenityName);
                List<Booking> updated = new ArrayList<>(bookings);
                for (int index = 0; index < updated.size(); index++) {
                    if (updated.get(index).getId() == bookingId) updated.set(index, cancelled);
                }
                state.setValue(UiState.success(updated));
                cancellation.setValue(UiState.success(cancelled));
            }

            @Override
            public void onError(DataError error) {
                cancellation.setValue(UiState.failure(error, null));
            }
        });
    }

    public void consumeCancellationResult() {
        if (!isCancelling()) cancellation.setValue(UiState.idle());
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
