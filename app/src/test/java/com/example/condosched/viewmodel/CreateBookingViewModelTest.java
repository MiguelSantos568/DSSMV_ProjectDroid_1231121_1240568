package com.example.condosched.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Resident;
import com.example.condosched.repository.BookingRepository;
import com.example.condosched.repository.RepositoryCallback;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class CreateBookingViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();

    private Amenity amenity() {
        Amenity amenity = new Amenity();
        amenity.setId(3);
        amenity.setActive(true);
        amenity.setCapacity(10);
        amenity.setOpeningTime(LocalTime.of(8, 0));
        amenity.setClosingTime(LocalTime.of(20, 0));
        return amenity;
    }

    @Test
    public void reloadAfterRotationDoesNotResetChosenDateOrLoadResidentsAgain() {
        FakeRepository repo = new FakeRepository();
        CreateBookingViewModel model = new CreateBookingViewModel(repo);
        model.load(1, amenity());
        model.setDate(model.getDate().getValue().plusDays(2));
        java.time.LocalDate chosen = model.getDate().getValue();
        model.load(1, amenity());
        assertEquals(1, repo.loads);
        assertEquals(chosen, model.getDate().getValue());
    }

    @Test
    public void rejectsUnknownResidentAndInvalidPeopleWithoutWriting() {
        FakeRepository repo = new FakeRepository();
        CreateBookingViewModel model = new CreateBookingViewModel(repo);
        model.load(1, amenity());
        model.submit(5, "1", "");
        assertEquals(DataError.RESIDENT_REQUIRED, model.getResult().getValue().getError());
        model.submit(9, "", "");
        assertEquals(DataError.INVALID_GUEST_COUNT, model.getResult().getValue().getError());
        model.submit(9, "11", "");
        assertEquals(DataError.INVALID_GUEST_COUNT, model.getResult().getValue().getError());
        assertEquals(0, repo.writes);
    }

    @Test
    public void preventsDuplicateTapsAndResubmissionAfterSuccess() {
        FakeRepository repo = new FakeRepository();
        CreateBookingViewModel model = new CreateBookingViewModel(repo);
        model.load(1, amenity());
        model.submit(9, "2", "");
        model.submit(9, "2", "");
        model.retryResidents();
        assertEquals(1, repo.writes);
        assertEquals(1, repo.loads);
        Booking saved = new Booking();
        saved.setId(25);
        repo.pending.onSuccess(saved);
        model.submit(9, "2", "");
        assertEquals(1, repo.writes);
        assertSame(saved, model.getResult().getValue().getData());
    }

    @Test
    public void conflictKeepsFormEditableAndDoesNotPretendSuccess() {
        FakeRepository repo = new FakeRepository();
        CreateBookingViewModel model = new CreateBookingViewModel(repo);
        model.load(1, amenity());
        model.submit(9, "2", "");
        repo.pending.onError(DataError.BOOKING_CONFLICT);
        assertEquals(UiState.Status.ERROR, model.getResult().getValue().getStatus());
        model.setStart(LocalTime.of(10, 0));
        model.setEnd(LocalTime.of(11, 0));
        model.submit(9, "2", "");
        assertEquals(2, repo.writes);
    }

    private static class FakeRepository extends BookingRepository {
        int loads;
        int writes;
        RepositoryCallback<Booking> pending;

        @Override
        public void loadResidents(long unitId, RepositoryCallback<List<Resident>> callback) {
            loads++;
            Resident resident = new Resident();
            resident.setId(9);
            resident.setUnitId(unitId);
            callback.onSuccess(Collections.singletonList(resident));
        }

        @Override
        public void create(long unitId, long amenityId, long residentId, LocalDateTime start,
                           LocalDateTime end, int people, String notes, RepositoryCallback<Booking> callback) {
            writes++;
            pending = callback;
        }
    }
}
