package com.example.condosched.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class WeatherService {

    private static final double LAT = 41.15;
    private static final double LON = -8.61024;

    /** @param days 1-16 */
    public static List<String> getWeather(int days) throws IOException, JSONException {
        URL url = new URL("https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + LAT
                + "&longitude=" + LON
                + "&daily=weather_code,temperature_2m_max,temperature_2m_min,"
                + "precipitation_sum,precipitation_probability_max"
                + "&timezone=Europe%2FLisbon"
                + "&forecast_days=" + Math.max(1, Math.min(days, 16)));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String response;
        try {
            int status = conn.getResponseCode();
            InputStream stream = status / 100 == 2 ? conn.getInputStream() : conn.getErrorStream();
            try (Scanner s = new Scanner(stream, "UTF-8").useDelimiter("\\A")) {
                response = s.hasNext() ? s.next() : "";
            }
            if (status / 100 != 2) {
                throw new IOException("Weather API error " + status + ": " + response);
            }
        } finally {
            conn.disconnect();
        }

        JSONObject daily = new JSONObject(response).getJSONObject("daily");
        JSONArray dates = daily.getJSONArray("time");
        JSONArray codes = daily.getJSONArray("weather_code");

        List<String> forecast = new ArrayList<>();
        for (int i = 0; i < dates.length(); i++) {
            forecast.add(String.format(Locale.US,
                    "%s: %s",
                    dates.getString(i),
                    describe(codes.getInt(i))));
        }
        return forecast;
    }

    /*
    * weather code to Portuguese description.
    */
    private static String describe(int code) {
        switch (code) {
            case 0:  return "Céu limpo";
            case 1:  return "Predominantemente limpo";
            case 2:  return "Parcialmente nublado";
            case 3:  return "Encoberto";
            case 45: case 48: return "Nevoeiro";
            case 51: case 53: case 55: return "Garoa";
            case 56: case 57: return "Garoa gelada";
            case 61: return "Chuva fraca";
            case 63: return "Chuva moderada";
            case 65: return "Chuva forte";
            case 66: case 67: return "Chuva gelada";
            case 71: case 73: case 75: case 77: return "Neve";
            case 80: return "Aguaceiros fracos";
            case 81: return "Aguaceiros moderados";
            case 82: return "Aguaceiros fortes";
            case 85: case 86: return "Aguaceiros de neve";
            case 95: return "Trovoada";
            case 96: case 99: return "Trovoada com granizo";
            default: return "Desconhecido";
        }
    }
}