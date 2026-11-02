package com.example.condosched.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.condosched.MainActivity;
import com.example.condosched.R;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.DTO.HabitationProfileDTO;
import com.example.condosched.network.HabitationProfileService;
import com.example.condosched.session.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Habitation home = (Habitation) getIntent().getSerializableExtra("habitation");
        if (home == null || home.unitId <= 0) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_register);
        com.example.condosched.ui.ScreenInsets.apply(this);
        ((TextView) findViewById(R.id.habitationSummary)).setText(getString(
                R.string.home_summary, home.condominium, home.building, home.floor, home.unit));
        EditText name = findViewById(R.id.nameInput);
        EditText email = findViewById(R.id.emailInput);
        TextView status = findViewById(R.id.registrationStatus);
        View enter = findViewById(R.id.registerButton);
        View rescan = findViewById(R.id.rescanButton);
        findViewById(R.id.registerButton).setOnClickListener(v -> {
            String fullName = name.getText().toString().trim();
            String address = email.getText().toString().trim();
            if (fullName.isEmpty()) {
                name.setError(getString(R.string.name_required));
                name.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
                email.setError(getString(R.string.email_invalid));
                email.requestFocus();
                return;
            }
            enter.setEnabled(false);
            rescan.setEnabled(false);
            name.setEnabled(false);
            email.setEnabled(false);
            status.setText(R.string.profile_saving);
            new HabitationProfileService().create(home.unitId, fullName, address,
                    new HabitationProfileService.ProfileCallback() {
                        @Override public void onSuccess(HabitationProfileDTO profile) {
                            if (isFinishing() || isDestroyed()) return;
                            // If another device created the profile first, keep that existing profile.
                            new SessionManager(RegisterActivity.this).save(home,
                                    profile.getFullName(), profile.getEmail());
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                        @Override public void onError(String message) {
                            if (isFinishing() || isDestroyed()) return;
                            enter.setEnabled(true);
                            rescan.setEnabled(true);
                            name.setEnabled(true);
                            email.setEnabled(true);
                            status.setText(message);
                        }
                    });
        });
        findViewById(R.id.rescanButton).setOnClickListener(v -> finish());
    }
}
