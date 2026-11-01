
package com.example.condosched.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ResidentService {

    private static final String BASE_URL =
            "https://klbqaciuiarexehsjqrq.supabase.co";

    public interface ResidentCallback {
        void onSuccess(JSONObject resident);
        void onError(String message);
    }

    // Guardar o perfil do residente (POST)
    public void createResident(
            String accessToken,
            String userId,
            String fullName,
            long unitId,
            ResidentCallback callback
    ) {
        JSONObject body = new JSONObject();

        try {
            body.put("id", userId);
            body.put("full_name", fullName);
            body.put("unit_id", unitId);
        } catch (Exception e) {
            callback.onError(e.getMessage());
            return;
        }

        request(
                "POST",
                "/rest/v1/residents",
                accessToken,
                body,
                callback
        );
    }

    // Consultar o perfil do residente (GET)
    public void getResident(
            String accessToken,
            String userId,
            ResidentCallback callback
    ) {
        request(
                "GET",
                "/rest/v1/residents?id=eq." + userId
                        + "&select=*",
                accessToken,
                null,
                callback
        );
    }

    // Comunicação HTTP com o Supabase
    private void request(
            String method,
            String path,
            String accessToken,
            JSONObject body,
            ResidentCallback callback
    ) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL + path);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                connection.setRequestProperty(
                        "apikey",
                        SupabaseClient.SUPABASE_KEY
                );
                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + accessToken
                );
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );
                connection.setRequestProperty(
                        "Prefer",
                        "return=representation"
                );

                if (body != null) {
                    connection.setDoOutput(true);

                    byte[] data = body.toString()
                            .getBytes(StandardCharsets.UTF_8);

                    connection.getOutputStream().write(data);
                }

                int status = connection.getResponseCode();

                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String response = "";

                if (stream != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] chunk = new byte[4096];
                    int count;
                    while ((count = stream.read(chunk)) != -1) {
                        buffer.write(chunk, 0, count);
                    }
                    response = new String(
                            buffer.toByteArray(),
                            StandardCharsets.UTF_8
                    );
                    stream.close();
                }

                if (status >= 200 && status < 300) {
                    String result = response;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            // Supabase REST devolve um array
                            org.json.JSONArray array =
                                    new org.json.JSONArray(result);

                            if (array.length() > 0) {
                                callback.onSuccess(array.getJSONObject(0));
                            } else {
                                callback.onError("Perfil não encontrado");
                            }
                        } catch (Exception e) {
                            callback.onError(e.getMessage());
                        }
                    });
                } else {
                    String error = "Erro HTTP " + status + ": " + response;

                    new Handler(Looper.getMainLooper()).post(
                            () -> callback.onError(error)
                    );
                }

            } catch (Exception e) {
                String error = e.getMessage();

                new Handler(Looper.getMainLooper()).post(
                        () -> callback.onError(error)
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
