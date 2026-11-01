package com.example.condosched;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.condosched.auth.LoginActivity;
import com.example.condosched.domain.Habitation;
import com.example.condosched.session.SessionManager;
import com.example.condosched.ui.*;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        SessionManager session = new SessionManager(this);
        if (!session.hasSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        Habitation home = session.getHabitation();
        ((TextView) findViewById(R.id.homeCondoName)).setText(home.condominium);
        ((TextView) findViewById(R.id.homeUnitInfo)).setText(getString(
                R.string.unit_summary, home.building, home.floor, home.unit));
        findViewById(R.id.homeProfileImage).setOnClickListener(v -> open(ProfileActivity.class));
        findViewById(R.id.homeCondoCard).setOnClickListener(v -> open(CondominiumActivity.class));
        findViewById(R.id.button1).setOnClickListener(v -> open(CondominiumActivity.class));
        findViewById(R.id.button2).setOnClickListener(v -> open(CreditsActivity.class));
        findViewById(R.id.homeMenuButton).setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            menu.getMenu().add(R.string.take_photo).setOnMenuItemClickListener(item -> {
                open(CameraActivity.class);
                return true;
            });
            menu.show();
        });
        BottomNavigation.bind(this, R.id.navHome);
    }
    private void open(Class<?> target) { startActivity(new Intent(this, target)); }
}
