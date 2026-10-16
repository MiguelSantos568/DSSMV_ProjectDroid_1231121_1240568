
package com.example.condosched.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CondominiumService {

    private static final String API_URL =
            "http://193.136.62.24/thecondo/api/condominiums";

    public interface CondominiumCallback {
        void onSuccess(JSONArray condominiums);
        void onError(String message);
    }

    public void getCondominiums(CondominiumCallback callback) {

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_URL);

                connection = (HttpURLConnection)
                        url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int status = connection.getResponseCode();

                if (status != 200) {
                    throw new Exception("HTTP " + status);
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONArray condominiums =
                        new JSONArray(response.toString());

                new Handler(Looper.getMainLooper()).post(
                        () -> callback.onSuccess(condominiums)
                );

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(
                        () -> callback.onError(e.getMessage())
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
