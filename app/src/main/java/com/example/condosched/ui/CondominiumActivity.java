package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.example.condosched.R;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HomeSession;

public class CondominiumActivity extends SessionActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        setContentView(R.layout.activity_condominium);
        ScreenInsets.apply(this);
        onSessionChanged(currentSession());
        findViewById(R.id.viewAmenitiesButton).setOnClickListener(v ->
                startActivity(new Intent(this, AmenitiesActivity.class)));
        BottomNavigation.bind(this, R.id.navCondo);
    }

    @Override
    protected void onSessionChanged(HomeSession session) {
        Habitation home = session.getHome();
        ((TextView) findViewById(R.id.condominiumName)).setText(home.condominium);
        ((TextView) findViewById(R.id.condominiumHome)).setText(getString(
                R.string.unit_summary, home.building, home.floor, home.unit));
    }
}
