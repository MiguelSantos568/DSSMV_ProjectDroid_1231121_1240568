package com.example.condosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.condosched.R;
import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.HabitationProfile;
import com.example.condosched.domain.DataError;
import com.example.condosched.viewmodel.RegisterViewModel;
import com.example.condosched.viewmodel.UiState;

public class RegisterActivity extends AppCompatActivity {
    public static final String EXTRA_HOME = "habitation";
    private EditText name, email;
    private TextView status;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Object extra = getIntent().getSerializableExtra(EXTRA_HOME);
        if (!(extra instanceof Habitation) || ((Habitation) extra).unitId <= 0) {
            finish();
            return;
        }
        Habitation home = (Habitation) extra;
        setContentView(R.layout.activity_register);
        ScreenInsets.apply(this);
        ((TextView) findViewById(R.id.habitationSummary)).setText(getString(
                R.string.home_summary, home.condominium, home.building, home.floor, home.unit));
        name = findViewById(R.id.nameInput);
        email = findViewById(R.id.emailInput);
        status = findViewById(R.id.registrationStatus);
        RegisterViewModel viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        viewModel.getState().observe(this, this::render);
        findViewById(R.id.registerButton).setOnClickListener(v ->
                viewModel.submit(home, name.getText().toString(), email.getText().toString()));
        findViewById(R.id.rescanButton).setOnClickListener(v -> finish());
    }

    private void render(UiState<HabitationProfile> state) {
        boolean enabled = state.getStatus() != UiState.Status.LOADING;
        name.setEnabled(enabled);
        email.setEnabled(enabled);
        findViewById(R.id.registerButton).setEnabled(enabled);
        findViewById(R.id.rescanButton).setEnabled(enabled);
        if (!enabled) status.setText(R.string.profile_saving);
        else if (state.getStatus() == UiState.Status.ERROR) {
            int message = UiErrors.message(state.getError());
            status.setText(message);
            if (state.getError() == DataError.INVALID_NAME) name.setError(getString(message));
            if (state.getError() == DataError.INVALID_EMAIL) email.setError(getString(message));
        } else if (state.getStatus() == UiState.Status.SUCCESS) {
            startActivity(new Intent(this, MainActivity.class).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }
}