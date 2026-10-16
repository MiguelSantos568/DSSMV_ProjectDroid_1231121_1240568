package com.example.condosched.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.condosched.MainActivity;
import com.example.condosched.R;
import com.example.condosched.network.AuthService;

public class LoginActivity extends Activity {

    private EditText emailInput, passwordInput;
    private Button loginButton, goToRegisterButton;
    private AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);

        loginButton.setOnClickListener(v -> login());

        goToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preenche todos os campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        authService.login(email, password, new AuthService.LoginCallback() {
            @Override
            public void onSuccess() {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Login realizado com sucesso!",
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}