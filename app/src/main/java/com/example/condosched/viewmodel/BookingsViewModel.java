package com.example.condosched.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.DataError;
import com.example.condosched.repository.BookingRepository;
import com.example.condosched.repository.RepositoryCallback;
import java.util.List;

public class BookingsViewModel extends ViewModel {
    private final BookingRepository repository = new BookingRepository();
    private final MutableLiveData<UiState<List<Booking>>> state = new MutableLiveData<>(UiState.idle());

    public LiveData<UiState<List<Booking>>> getState() {
        return state;
    }

    public void load(long unitId) {
        if (state.getValue().getStatus() == UiState.Status.LOADING) return;
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

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
