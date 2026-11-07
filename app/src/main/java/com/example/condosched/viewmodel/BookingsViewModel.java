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
    private final MutableLiveData<UiState<Long>> deletion = new MutableLiveData<>(UiState.idle());

    public BookingsViewModel() {
        this(new BookingRepository());
    }

    BookingsViewModel(BookingRepository repository) {
        this.repository = repository;
    }

    public LiveData<UiState<List<Booking>>> getState() {
        return state;
    }

    public LiveData<UiState<Long>> getDeletion() {
        return deletion;
    }

    public boolean isDeleting() {
        return deletion.getValue().getStatus() == UiState.Status.LOADING;
    }

    public void load(long unitId) {
        if (state.getValue().getStatus() == UiState.Status.LOADING || isDeleting()) return;
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

    public void deleteBooking(long unitId, long bookingId) {
        if (state.getValue().getStatus() != UiState.Status.SUCCESS || isDeleting()) return;
        List<Booking> bookings = state.getValue().getData();
        Booking selected = null;
        for (Booking booking : bookings) {
            if (booking.getId() == bookingId && booking.getUnitId() == unitId) selected = booking;
        }
        if (selected == null) return;
        deletion.setValue(UiState.loading(bookingId));
        repository.deleteBooking(unitId, bookingId, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long deletedId) {
                List<Booking> updated = new ArrayList<>(bookings);
                updated.removeIf(booking -> booking.getId() == deletedId);
                state.setValue(UiState.success(updated));
                deletion.setValue(UiState.success(deletedId));
            }

            @Override
            public void onError(DataError error) {
                deletion.setValue(UiState.failure(error, null));
            }
        });
    }

    public void consumeDeletionResult() {
        if (!isDeleting()) deletion.setValue(UiState.idle());
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
