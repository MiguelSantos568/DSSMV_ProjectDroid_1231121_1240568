package com.example.condosched.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.ViewModelStore;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.DataError;
import com.example.condosched.repository.BookingRepository;
import com.example.condosched.repository.RepositoryCallback;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class BookingsViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();

    private Booking booking(String status) {
        Booking booking = new Booking();
        booking.setId(25);
        booking.setUnitId(1);
        booking.setAmenityId(3);
        booking.setAmenityName("Sala");
        booking.setStatus(status);
        return booking;
    }

    @Test
    public void successfulCancellationKeepsHistoryAndAmenityName() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.cancelBooking(1, 25);
        assertTrue(model.isCancelling());
        repository.pending.onSuccess(booking("cancelled"));
        assertFalse(model.isCancelling());
        List<Booking> data = model.getState().getValue().getData();
        assertEquals(1, data.size());
        assertEquals("cancelled", data.get(0).getStatus());
        assertEquals("Sala", data.get(0).getAmenityName());
        assertFalse(data.get(0).canCancel());
        assertEquals(UiState.Status.SUCCESS, model.getCancellation().getValue().getStatus());
        model.consumeCancellationResult();
        assertEquals(UiState.Status.IDLE, model.getCancellation().getValue().getStatus());
        assertEquals(1, model.getState().getValue().getData().size());
    }

    @Test
    public void failedCancellationDoesNotMarkBookingAsCancelled() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.cancelBooking(1, 25);
        repository.pending.onError(DataError.BOOKING_CANCEL_FAILED);
        assertEquals("confirmed", model.getState().getValue().getData().get(0).getStatus());
        assertEquals(UiState.Status.ERROR, model.getCancellation().getValue().getStatus());
        assertEquals(DataError.BOOKING_CANCEL_FAILED, model.getCancellation().getValue().getError());
    }

    @Test
    public void duplicateTapsAndRefreshCannotInterruptCancellation() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.cancelBooking(1, 25);
        model.cancelBooking(1, 25);
        model.load(1);
        assertEquals(1, repository.cancellations);
        assertEquals(1, repository.loads);
    }

    @Test
    public void cancelledOrForeignOrUnknownBookingsCannotBeSubmitted() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.cancelBooking(2, 25);
        model.cancelBooking(1, 99);
        assertEquals(0, repository.cancellations);
        model.cancelBooking(1, 25);
        repository.pending.onSuccess(booking("cancelled"));
        model.cancelBooking(1, 25);
        assertEquals(1, repository.cancellations);
    }

    @Test
    public void clearingViewModelCancelsPendingRequest() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        ViewModelStore store = new ViewModelStore();
        store.put("bookings", model);
        model.load(1);
        model.cancelBooking(1, 25);
        store.clear();
        assertTrue(repository.cancelled);
    }

    private class FakeRepository extends BookingRepository {
        int loads;
        int cancellations;
        boolean cancelled;
        RepositoryCallback<Booking> pending;

        @Override
        public void loadForHome(long unitId, RepositoryCallback<List<Booking>> callback) {
            loads++;
            callback.onSuccess(Collections.singletonList(booking("confirmed")));
        }

        @Override
        public void cancelBooking(long unitId, long bookingId, RepositoryCallback<Booking> callback) {
            cancellations++;
            pending = callback;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
