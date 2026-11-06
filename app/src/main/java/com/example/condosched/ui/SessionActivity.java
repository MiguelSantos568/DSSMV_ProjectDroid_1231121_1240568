package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.domain.HomeSession;
import com.example.condosched.viewmodel.SessionViewModel;

/**
 * Verificação da sessão comum aos ecrãs que exigem uma habitação selecionada.
 */
public abstract class SessionActivity extends AppCompatActivity {
    protected SessionViewModel sessionViewModel;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        if (sessionViewModel.current() == null) {
            openAccess();
            return;
        }
        sessionViewModel.getSession().observe(this, session -> {
            if (session == null) openAccess();
            else onSessionChanged(session);
        });
    }

    protected HomeSession currentSession() {
        return sessionViewModel.current();
    }

    protected void onSessionChanged(HomeSession session) {
    }

    private void openAccess() {
        if (isFinishing()) return;
        startActivity(new Intent(this, AccessActivity.class).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}