package com.example.condosched.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Habitation;
import com.example.condosched.repository.AmenityRepository;
import com.example.condosched.domain.DataError;
import com.example.condosched.repository.RepositoryCallback;

import java.util.List;

/**
 * T6: holds screen state without keeping an Activity or Android View reference.
 */
public class AmenitiesViewModel extends ViewModel {
    private final AmenityRepository repository;
    private final MutableLiveData<AmenitiesState> state = new MutableLiveData<>();
    private Habitation currentHome;

    public AmenitiesViewModel() {
        this(new AmenityRepository());
    }

    AmenitiesViewModel(AmenityRepository repository) {
        this.repository = repository;
    }

    // The Activity can observe state, but only the ViewModel can change it.
    public LiveData<AmenitiesState> getState() {
        return state;
    }

    public void load(Habitation home) {
        // ViewModelProvider returns this same instance after rotation: do not repeat the HTTP request.
        if (currentHome != null && currentHome.unitId == home.unitId && state.getValue() != null) return;
        currentHome = home;
        fetch();
    }

    public void retry() {
        if (currentHome != null) fetch();
    }

    private void fetch() {
        state.setValue(AmenitiesState.loading(currentHome));
        if (currentHome.condominiumId > 0) {
            loadAmenities();
        } else {
            repository.findHome(currentHome.unitId, new RepositoryCallback<Habitation>() {
                @Override
                public void onSuccess(Habitation home) {
                    currentHome = home;
                    loadAmenities();
                }

                @Override
                public void onError(DataError error) {
                    state.setValue(AmenitiesState.failure(currentHome, error));
                }
            });
        }
    }

    private void loadAmenities() {
        repository.getByCondominium(currentHome.condominiumId, new RepositoryCallback<List<Amenity>>() {
            @Override
            public void onSuccess(List<Amenity> amenities) {
                state.setValue(AmenitiesState.success(currentHome, amenities));
            }

            @Override
            public void onError(DataError error) {
                state.setValue(AmenitiesState.failure(currentHome, error));
            }
        });
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
