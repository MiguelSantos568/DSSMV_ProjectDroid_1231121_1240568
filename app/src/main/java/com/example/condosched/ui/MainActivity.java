package com.example.condosched.ui;

import com.example.condosched.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HomeSession;

public class MainActivity extends SessionActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ScreenInsets.apply(this);
        onSessionChanged(currentSession());
        findViewById(R.id.homeProfileImage).setOnClickListener(v -> open(ProfileActivity.class));
        findViewById(R.id.homeCondoCard).setOnClickListener(v -> open(CondominiumActivity.class));
        findViewById(R.id.homeAmenitiesButton).setOnClickListener(v -> open(AmenitiesActivity.class));
        findViewById(R.id.homeCreditsButton).setOnClickListener(v -> open(CreditsActivity.class));
        findViewById(R.id.homeMenuButton).setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            menu.getMenu().add(R.string.take_photo).setOnMenuItemClickListener(item -> {
                open(CameraActivity.class);
                return true;
            });
            menu.getMenu().add(R.string.view_weather).setOnMenuItemClickListener(item -> {
                open(WeatherActivity.class);
                return true;
            });
            menu.show();
        });
        BottomNavigation.bind(this, R.id.navHome);
    }

    @Override
    protected void onSessionChanged(HomeSession session) {
        Habitation home = session.getHome();
        ((TextView) findViewById(R.id.homeCondoName)).setText(home.condominium);
        ((TextView) findViewById(R.id.homeUnitInfo)).setText(getString(
                R.string.unit_summary, home.building, home.floor, home.unit));
    }

    private void open(Class<?> target) {
        startActivity(new Intent(this, target));
    }
}
