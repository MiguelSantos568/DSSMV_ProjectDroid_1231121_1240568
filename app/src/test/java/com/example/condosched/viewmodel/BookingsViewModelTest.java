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
    public void successfulDeletionRemovesBookingFromList() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.deleteBooking(1, 25);
        assertTrue(model.isDeleting());
        repository.pending.onSuccess(25L);
        assertFalse(model.isDeleting());
        List<Booking> data = model.getState().getValue().getData();
        assertTrue(data.isEmpty());
        assertEquals(UiState.Status.SUCCESS, model.getDeletion().getValue().getStatus());
        model.consumeDeletionResult();
        assertEquals(UiState.Status.IDLE, model.getDeletion().getValue().getStatus());
        assertTrue(model.getState().getValue().getData().isEmpty());
    }

    @Test
    public void failedDeletionKeepsBookingInList() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.deleteBooking(1, 25);
        repository.pending.onError(DataError.BOOKING_DELETE_FAILED);
        assertEquals("confirmed", model.getState().getValue().getData().get(0).getStatus());
        assertEquals(UiState.Status.ERROR, model.getDeletion().getValue().getStatus());
        assertEquals(DataError.BOOKING_DELETE_FAILED, model.getDeletion().getValue().getError());
    }

    @Test
    public void duplicateTapsAndRefreshCannotInterruptDeletion() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.deleteBooking(1, 25);
        model.deleteBooking(1, 25);
        model.load(1);
        assertEquals(1, repository.deletions);
        assertEquals(1, repository.loads);
    }

    @Test
    public void foreignOrUnknownOrDeletedBookingsCannotBeSubmitted() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        model.load(1);
        model.deleteBooking(2, 25);
        model.deleteBooking(1, 99);
        assertEquals(0, repository.deletions);
        model.deleteBooking(1, 25);
        repository.pending.onSuccess(25L);
        model.deleteBooking(1, 25);
        assertEquals(1, repository.deletions);
    }

    @Test
    public void clearingViewModelCancelsPendingRequest() {
        FakeRepository repository = new FakeRepository();
        BookingsViewModel model = new BookingsViewModel(repository);
        ViewModelStore store = new ViewModelStore();
        store.put("bookings", model);
        model.load(1);
        model.deleteBooking(1, 25);
        store.clear();
        assertTrue(repository.cancelled);
    }

    private class FakeRepository extends BookingRepository {
        int loads;
        int deletions;
        boolean cancelled;
        RepositoryCallback<Long> pending;

        @Override
        public void loadForHome(long unitId, RepositoryCallback<List<Booking>> callback) {
            loads++;
            callback.onSuccess(Collections.singletonList(booking("confirmed")));
        }

        @Override
        public void deleteBooking(long unitId, long bookingId, RepositoryCallback<Long> callback) {
            deletions++;
            pending = callback;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
