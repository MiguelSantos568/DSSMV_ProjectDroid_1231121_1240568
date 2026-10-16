
package com.example.condosched.auth;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.condosched.R;
import com.example.condosched.network.AuthService;

public class RegisterActivity extends Activity {

    private EditText emailInput, passwordInput;
    private Button registerButton;
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> register());
    }

    private void register() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preenche todos os campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);

        authService.register(email, password,
                new AuthService.LoginCallback() {

                    @Override
                    public void onSuccess() {
                        registerButton.setEnabled(true);

                        Toast.makeText(RegisterActivity.this,
                                "Conta criada com sucesso!",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        registerButton.setEnabled(true);

                        Toast.makeText(RegisterActivity.this,
                                message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
