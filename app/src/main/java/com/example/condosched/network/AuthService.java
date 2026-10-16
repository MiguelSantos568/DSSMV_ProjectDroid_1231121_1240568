package com.example.condosched.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AuthService {

    public interface LoginCallback {
        void onSuccess();
        void onError(String message);
    }

    public void login(String email, String password,
                      LoginCallback callback) {

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(SupabaseClient.AUTH_URL
                        + "/token?grant_type=password");

                connection = (HttpURLConnection)
                        url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "apikey", SupabaseClient.SUPABASE_KEY);
                connection.setRequestProperty(
                        "Content-Type", "application/json");

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);

                try (OutputStream output =
                             connection.getOutputStream()) {
                    output.write(body.toString().getBytes(
                            StandardCharsets.UTF_8));
                }

                int status = connection.getResponseCode();

                if (status == 200) {
                    new Handler(Looper.getMainLooper())
                            .post(callback::onSuccess);
                } else {
                    new Handler(Looper.getMainLooper())
                            .post(() -> callback.onError(
                                    "Email ou password incorretos"));
                }

            } catch (Exception e) {
                new Handler(Looper.getMainLooper())
                        .post(() -> callback.onError(
                                "Erro de ligação: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }


    public void register(String email, String password,
                         LoginCallback callback) {

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        SupabaseClient.AUTH_URL + "/signup"
                );

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "apikey", SupabaseClient.SUPABASE_KEY);
                connection.setRequestProperty(
                        "Content-Type", "application/json");

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.toString().getBytes(
                            StandardCharsets.UTF_8));
                }

                int status = connection.getResponseCode();

                if (status == 200 || status == 201) {
                    new Handler(Looper.getMainLooper())
                            .post(callback::onSuccess);
                } else {
                    new Handler(Looper.getMainLooper())
                            .post(() -> callback.onError(
                                    "Não foi possível criar a conta"));
                }

            } catch (Exception e) {
                new Handler(Looper.getMainLooper())
                        .post(() -> callback.onError(
                                "Erro de ligação: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}

