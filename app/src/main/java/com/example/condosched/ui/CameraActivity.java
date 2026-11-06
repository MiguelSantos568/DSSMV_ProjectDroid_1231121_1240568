package com.example.condosched.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.viewmodel.CameraViewModel;
import com.example.condosched.viewmodel.UiState;

public class CameraActivity extends SessionActivity {
    private CameraViewModel viewModel;
    private final ActivityResultLauncher<Void> camera = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(), image -> viewModel.capture(image));
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), allowed -> {
                if (allowed) openCamera();
                else message(R.string.camera_permission);
            });
    private final ActivityResultLauncher<String> storagePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), allowed -> {
                if (allowed) viewModel.save();
                else message(R.string.storage_permission);
            });

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        setContentView(R.layout.activity_camera);
        ScreenInsets.apply(this);
        viewModel = new ViewModelProvider(this).get(CameraViewModel.class);
        viewModel.getState().observe(this, result -> {
            boolean saving = result.getStatus() == UiState.Status.LOADING;
            ((ImageView) findViewById(R.id.photoPreview)).setImageBitmap(result.getData());
            findViewById(R.id.takePhotoButton).setEnabled(!saving);
            findViewById(R.id.savePhotoButton).setEnabled(result.getData() != null && !saving);
            ((TextView) findViewById(R.id.photoStatus)).setText(saving ? R.string.photo_saving
                    : result.getStatus() == UiState.Status.ERROR ? R.string.photo_error : R.string.photo_hint);
        });
        viewModel.getSavedPhoto().observe(this, uri -> {
            if (uri != null) {
                message(R.string.photo_saved);
                viewModel.consumeSavedPhoto();
            }
        });
        findViewById(R.id.photoBack).setOnClickListener(v -> finish());
        findViewById(R.id.takePhotoButton).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                openCamera();
            else cameraPermission.launch(Manifest.permission.CAMERA);
        });
        findViewById(R.id.savePhotoButton).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT <= 28 && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else viewModel.save();
        });
    }

    private void openCamera() {
        try {
            camera.launch(null);
        } catch (ActivityNotFoundException error) {
            message(R.string.camera_unavailable);
        }
    }

    private void message(int resource) {
        Toast.makeText(this, resource, Toast.LENGTH_LONG).show();
    }
}