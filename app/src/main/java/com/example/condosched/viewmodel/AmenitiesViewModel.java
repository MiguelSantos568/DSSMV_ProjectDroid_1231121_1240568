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
import java.util.Collections;

/**
 * Mantém os dados do ecrã sem guardar referências à Activity ou às Views.
 */
public class AmenitiesViewModel extends ViewModel {
    private final AmenityRepository repository;
    private final MutableLiveData<UiState<List<Amenity>>> state = new MutableLiveData<>(UiState.idle());
    private Habitation currentHome;

    public AmenitiesViewModel() {
        this(new AmenityRepository());
    }

    AmenitiesViewModel(AmenityRepository repository) {
        this.repository = repository;
    }

    public LiveData<UiState<List<Amenity>>> getState() {
        return state;
    }

    public void load(Habitation home) {
        // A rotação reutiliza o ViewModel sem repetir o pedido HTTP.
        if (currentHome != null && currentHome.unitId == home.unitId) return;
        currentHome = home;
        fetch();
    }

    public void retry() {
        if (currentHome != null) fetch();
    }

    private void fetch() {
        state.setValue(UiState.loading(Collections.emptyList()));
        repository.getByCondominium(currentHome.condominiumId, new RepositoryCallback<List<Amenity>>() {
            @Override
            public void onSuccess(List<Amenity> amenities) {
                state.setValue(UiState.success(amenities));
            }

            @Override
            public void onError(DataError error) {
                state.setValue(UiState.failure(error, Collections.emptyList()));
            }
        });
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
