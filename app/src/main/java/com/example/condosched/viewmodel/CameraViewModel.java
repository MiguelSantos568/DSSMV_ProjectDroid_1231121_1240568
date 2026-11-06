package com.example.condosched.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.condosched.domain.DataError;
import com.example.condosched.repository.PhotoRepository;
import com.example.condosched.repository.RepositoryCallback;

public class CameraViewModel extends AndroidViewModel {
    private final PhotoRepository photos;
    private final MutableLiveData<UiState<Bitmap>> state = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<Uri> savedPhoto = new MutableLiveData<>();

    public CameraViewModel(Application application) {
        super(application);
        photos = new PhotoRepository(application);
    }

    public LiveData<UiState<Bitmap>> getState() {
        return state;
    }

    public LiveData<Uri> getSavedPhoto() {
        return savedPhoto;
    }

    public void capture(Bitmap image) {
        if (image != null) state.setValue(UiState.success(image));
    }

    public void save() {
        UiState<Bitmap> current = state.getValue();
        if (current.getStatus() == UiState.Status.LOADING || current.getData() == null) return;
        Bitmap image = current.getData();
        state.setValue(UiState.loading(image));
        photos.save(image, new RepositoryCallback<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                state.setValue(UiState.success(image));
                savedPhoto.setValue(uri);
            }

            @Override
            public void onError(DataError error) {
                state.setValue(UiState.failure(error, image));
            }
        });
    }

    public void consumeSavedPhoto() {
        savedPhoto.setValue(null);
    }

    @Override
    protected void onCleared() {
        photos.close();
    }
}