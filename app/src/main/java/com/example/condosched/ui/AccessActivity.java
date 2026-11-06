package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.viewmodel.AccessViewModel;
import com.example.condosched.viewmodel.SessionViewModel;
import com.example.condosched.viewmodel.UiState;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class AccessActivity extends AppCompatActivity {
    private AccessViewModel viewModel;
    private TextView status;
    private View scanButton;
    private final ActivityResultLauncher<ScanOptions> scanner = registerForActivityResult(
            new ScanContract(), result -> {
                if (result.getContents() == null) status.setText(R.string.qr_cancelled);
                else viewModel.scan(result.getContents());
            });

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (new ViewModelProvider(this).get(SessionViewModel.class).current() != null) {
            openHome();
            return;
        }
        setContentView(R.layout.activity_access);
        ScreenInsets.apply(this);
        status = findViewById(R.id.qrStatus);
        scanButton = findViewById(R.id.scanQrButton);
        viewModel = new ViewModelProvider(this).get(AccessViewModel.class);
        viewModel.getState().observe(this, this::render);
        scanButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt(getString(R.string.qr_prompt));
            options.setBeepEnabled(false);
            options.setOrientationLocked(false);
            scanner.launch(options);
        });
    }

    private void render(UiState<AccessViewModel.Result> state) {
        scanButton.setEnabled(state.getStatus() != UiState.Status.LOADING);
        if (state.getStatus() == UiState.Status.LOADING) status.setText(R.string.qr_loading);
        else if (state.getStatus() == UiState.Status.ERROR) status.setText(UiErrors.message(state.getError()));
        else if (state.getStatus() == UiState.Status.IDLE) status.setText(R.string.qr_ready);
        else {
            AccessViewModel.Result result = state.getData();
            viewModel.consumeResult();
            if (result.needsRegistration()) startActivity(new Intent(this, RegisterActivity.class)
                    .putExtra(RegisterActivity.EXTRA_HOME, result.getHome()));
            else openHome();
        }
    }

    private void openHome() {
        startActivity(new Intent(this, MainActivity.class).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}