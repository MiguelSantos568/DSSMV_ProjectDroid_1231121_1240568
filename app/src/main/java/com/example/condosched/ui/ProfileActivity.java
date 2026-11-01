package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.condosched.R;
import com.example.condosched.auth.LoginActivity;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.DTO.HabitationProfileDTO;
import com.example.condosched.network.HabitationProfileService;
import com.example.condosched.session.SessionManager;

public class ProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        SessionManager session = new SessionManager(this);
        if (!session.hasSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_profile);
        ScreenInsets.apply(this);
        Habitation home = session.getHabitation();
        setText(R.id.profileName, session.getName());
        setText(R.id.profileEmail, session.getEmail());
        setText(R.id.profileCondominium, home.condominium);
        setText(R.id.profileBuilding, home.building);
        setText(R.id.profileFloor, home.floor);
        setText(R.id.profileUnit, home.unit);
        BottomNavigation.bind(this, R.id.navProfile);
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            session.clear();
            startActivity(new Intent(this, LoginActivity.class).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
        refreshProfile(session, home);
    }

    private void refreshProfile(SessionManager session, Habitation home) {
        new HabitationProfileService().find(home.unitId, new HabitationProfileService.ProfileCallback() {
            @Override public void onSuccess(HabitationProfileDTO profile) {
                if (isFinishing() || isDestroyed()) return;
                if (profile == null) {
                    session.clear();
                    Toast.makeText(ProfileActivity.this, R.string.profile_missing, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                    return;
                }
                session.save(home, profile.getFullName(), profile.getEmail());
                setText(R.id.profileName, profile.getFullName());
                setText(R.id.profileEmail, profile.getEmail());
            }
            @Override public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(ProfileActivity.this, R.string.profile_offline, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setText(int id, String value) { ((TextView) findViewById(id)).setText(value); }
}
