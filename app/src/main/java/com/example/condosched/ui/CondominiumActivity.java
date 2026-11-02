package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.condosched.R;
import com.example.condosched.auth.LoginActivity;
import com.example.condosched.domain.Habitation;
import com.example.condosched.session.SessionManager;

public class CondominiumActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        SessionManager session = new SessionManager(this);
        if (!session.hasSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.condominiumlist);
        ScreenInsets.apply(this);
        Habitation home = session.getHabitation();
        ((TextView) findViewById(R.id.condominiumName)).setText(home.condominium);
        ((TextView) findViewById(R.id.condominiumHome)).setText(getString(
                R.string.unit_summary, home.building, home.floor, home.unit));
        findViewById(R.id.viewAmenitiesButton).setOnClickListener(v ->
                startActivity(new Intent(this, AmenitiesActivity.class)));
        BottomNavigation.bind(this, R.id.navCondo);
    }
}