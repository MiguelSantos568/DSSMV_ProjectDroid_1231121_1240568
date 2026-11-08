package com.example.condosched.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HomeSession;
import com.example.condosched.domain.DataError;
import com.example.condosched.viewmodel.ProfileViewModel;
import com.example.condosched.viewmodel.UiState;

public class ProfileActivity extends SessionActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isFinishing()) return;
        setContentView(R.layout.activity_profile);
        ScreenInsets.apply(this);
        BottomNavigation.bind(this, R.id.navProfile);
        onSessionChanged(currentSession());

        // Bind logout button
        findViewById(R.id.logoutButton).setOnClickListener(v -> sessionViewModel.logout());

        // Bind language selection button
        View languageButton = findViewById(R.id.languageButton);
        if (languageButton != null) {
            languageButton.setOnClickListener(this::showLanguageMenu);
        }

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getState().observe(this, result -> {
            if (result.getStatus() != UiState.Status.ERROR) return;
            boolean missing = result.getError() == DataError.NOT_FOUND;
            Toast.makeText(this, missing ? R.string.profile_missing : R.string.profile_offline, Toast.LENGTH_LONG).show();
            viewModel.consumeMessage();
            if (missing) sessionViewModel.logout();
        });
        viewModel.load();
    }

    @Override
    protected void onSessionChanged(HomeSession session) {
        Habitation home = session.getHome();
        text(R.id.profileName, session.getProfile().getFullName());
        text(R.id.profileEmail, session.getProfile().getEmail());
        text(R.id.profileCondominium, home.condominium);
        text(R.id.profileBuilding, home.building);
        text(R.id.profileFloor, home.floor);
        text(R.id.profileUnit, home.unit);
    }

    private void text(int id, String value) {
        TextView textView = findViewById(id);
        if (textView != null) {
            textView.setText(value);
        }
    }

    private void showLanguageMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        int current = Language.getCurrentIndex();

        for (int i = 0; i < Language.NAMES.length; i++) {
            menu.getMenu()
                    .add(1, i, i, Language.NAMES[i])
                    .setCheckable(true)
                    .setChecked(i == current);
        }
        menu.getMenu().setGroupCheckable(1, true, true);

        menu.setOnMenuItemClickListener(item -> {
            int which = item.getItemId();
            if (which != current) {
                Language.setLanguage(Language.TAGS[which]);
                // Refresh activity to apply the changed locale
                recreate();
            }
            return true;
        });
        menu.show();
    }
}