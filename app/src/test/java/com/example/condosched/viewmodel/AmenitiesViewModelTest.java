package com.example.condosched.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.ViewModelStore;
import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.DataError;
import com.example.condosched.repository.RepositoryCallback;
import com.example.condosched.domain.Habitation;
import com.example.condosched.repository.AmenityRepository;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class AmenitiesViewModelTest {
    @Rule
    public InstantTaskExecutorRule liveDataRule = new InstantTaskExecutorRule();
    private final Habitation home = new Habitation(1, 7, 8, "Condo", "Building", "1", "101");

    @Test
    public void repeatedLoadDuringAndAfterRequestDoesNotFetchAgain() {
        FakeRepository repository = new FakeRepository();
        AmenitiesViewModel model = new AmenitiesViewModel(repository);
        model.load(home);
        assertEquals(AmenitiesState.Status.LOADING, model.getState().getValue().getStatus());
        model.load(home);
        assertEquals(1, repository.requests);
        Amenity amenity = new Amenity();
        repository.pending.onSuccess(Collections.singletonList(amenity));
        model.load(home); // A recreated Activity calls load with the same home.
        assertEquals(1, repository.requests);
        assertEquals(AmenitiesState.Status.CONTENT, model.getState().getValue().getStatus());
        assertSame(amenity, model.getState().getValue().getAmenities().get(0));
    }

    @Test
    public void successfulEmptyResultHasItsOwnState() {
        FakeRepository repository = new FakeRepository();
        AmenitiesViewModel model = new AmenitiesViewModel(repository);
        model.load(home);
        repository.pending.onSuccess(Collections.emptyList());
        assertEquals(AmenitiesState.Status.EMPTY, model.getState().getValue().getStatus());
    }

    @Test
    public void errorIsExposedAndRetryStartsANewRequest() {
        FakeRepository repository = new FakeRepository();
        AmenitiesViewModel model = new AmenitiesViewModel(repository);
        model.load(home);
        repository.pending.onError(DataError.LOAD_FAILED);
        assertEquals(AmenitiesState.Status.ERROR, model.getState().getValue().getStatus());
        assertEquals(DataError.LOAD_FAILED, model.getState().getValue().getError());
        model.retry();
        assertEquals(2, repository.requests);
        assertEquals(AmenitiesState.Status.LOADING, model.getState().getValue().getStatus());
    }

    @Test
    public void resolvesOldSessionBeforeRequestingAmenities() {
        FakeRepository repository = new FakeRepository();
        AmenitiesViewModel model = new AmenitiesViewModel(repository);
        model.load(new Habitation(1, 0, 0, "Condo", "Building", "1", "101"));
        assertEquals(0, repository.requests);
        repository.homePending.onSuccess(home);
        assertEquals(1, repository.requests);
        assertEquals(7, repository.condominiumId);
        repository.pending.onSuccess(Collections.emptyList());
        assertSame(home, model.getState().getValue().getHome());
    }

    @Test
    public void clearingViewModelCancelsItsRepositoryRequest() {
        FakeRepository repository = new FakeRepository();
        AmenitiesViewModel model = new AmenitiesViewModel(repository);
        ViewModelStore store = new ViewModelStore();
        store.put("amenities", model);
        model.load(home);
        store.clear();
        assertTrue(repository.cancelled);
    }

    private static class FakeRepository extends AmenityRepository {
        int requests;
        long condominiumId;
        boolean cancelled;
        RepositoryCallback<java.util.List<Amenity>> pending;
        RepositoryCallback<Habitation> homePending;

        @Override
        public void getByCondominium(long id, RepositoryCallback<java.util.List<Amenity>> callback) {
            requests++;
            condominiumId = id;
            pending = callback;
        }

        @Override
        public void findHome(long unitId, RepositoryCallback<Habitation> callback) {
            homePending = callback;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
