package com.example.condosched.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.example.condosched.MainActivity;
import com.example.condosched.R;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HabitationQr;
import com.example.condosched.domain.DTO.HabitationProfileDTO;
import com.example.condosched.network.HabitationService;
import com.example.condosched.network.HabitationProfileService;
import com.example.condosched.session.SessionManager;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class LoginActivity extends AppCompatActivity {
    private TextView status;
    private View scanButton;
    private boolean loading;
    private final ActivityResultLauncher<ScanOptions> scanner = registerForActivityResult(
            new ScanContract(), result -> {
                if (result.getContents() == null) {
                    status.setText(R.string.qr_cancelled);
                    return;
                }
                try {
                    loadHabitation(HabitationQr.parse(result.getContents()));
                } catch (IllegalArgumentException e) {
                    status.setText(R.string.qr_invalid);
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (new SessionManager(this).hasSession()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);
        com.example.condosched.ui.ScreenInsets.apply(this);
        status = findViewById(R.id.qrStatus);
        scanButton = findViewById(R.id.scanQrButton);
        scanButton.setOnClickListener(v -> {
            if (loading) return;
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt(getString(R.string.qr_prompt));
            options.setBeepEnabled(false);
            options.setOrientationLocked(false);
            scanner.launch(options);
        });
    }

    private void loadHabitation(long id) {
        loading = true;
        scanButton.setEnabled(false);
        status.setText(R.string.qr_loading);
        new HabitationService().find(id, new HabitationService.Callback() {
            @Override public void onSuccess(Habitation home) {
                if (isFinishing() || isDestroyed()) return;
                loadProfile(home);
            }
            @Override public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                loading = false;
                scanButton.setEnabled(true);
                status.setText(message);
            }
        });
    }

    private void loadProfile(Habitation home) {
        status.setText(R.string.profile_loading);
        new HabitationProfileService().find(home.unitId, new HabitationProfileService.ProfileCallback() {
            @Override public void onSuccess(HabitationProfileDTO profile) {
                if (isFinishing() || isDestroyed()) return;
                loading = false;
                scanButton.setEnabled(true);
                status.setText(R.string.qr_ready);
                if (profile == null) {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class)
                            .putExtra("habitation", home));
                } else {
                    new SessionManager(LoginActivity.this).save(home, profile.getFullName(), profile.getEmail());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
            @Override public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                loading = false;
                scanButton.setEnabled(true);
                status.setText(message);
            }
        });
    }
}
