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
        assertEquals(UiState.Status.LOADING, model.getState().getValue().getStatus());
        model.load(home);
        assertEquals(1, repository.requests);
        Amenity amenity = new Amenity();
        repository.pending.onSuccess(Collections.singletonList(amenity));
        model.load(home); // A Activity recriada volta a pedir os dados da mesma casa.
        assertEquals(1, repository.requests);
        assertEquals(UiState.Status.SUCCESS, model.getState().getValue().getStatus());
        assertSame(amenity, model.getState().getValue().getData().get(0));
    }

    @Test
    public void emptyListIsSuccessful() {
        FakeRepository repository = new FakeRepository();
        AmenitiesViewModel model = new AmenitiesViewModel(repository);
        model.load(home);
        repository.pending.onSuccess(Collections.emptyList());
        assertEquals(UiState.Status.SUCCESS, model.getState().getValue().getStatus());
        assertTrue(model.getState().getValue().getData().isEmpty());
    }

    @Test
    public void errorIsExposedAndRetryStartsANewRequest() {
        FakeRepository repository = new FakeRepository();
        AmenitiesViewModel model = new AmenitiesViewModel(repository);
        model.load(home);
        repository.pending.onError(DataError.LOAD_FAILED);
        assertEquals(UiState.Status.ERROR, model.getState().getValue().getStatus());
        assertEquals(DataError.LOAD_FAILED, model.getState().getValue().getError());
        model.retry();
        assertEquals(2, repository.requests);
        assertEquals(UiState.Status.LOADING, model.getState().getValue().getStatus());
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
        boolean cancelled;
        RepositoryCallback<java.util.List<Amenity>> pending;

        @Override
        public void getByCondominium(long id, RepositoryCallback<java.util.List<Amenity>> callback) {
            requests++;
            pending = callback;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
