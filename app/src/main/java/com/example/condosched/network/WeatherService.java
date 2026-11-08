package com.example.condosched.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.core.util.Consumer;
import com.example.condosched.R;
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

import static android.provider.Settings.System.getString;

public final class WeatherService {

    private static final String API_Key = "663a7db0500f4a4885f00754260111";

    // Default position (Porto)
    public static final String DEFAULT_LAT = "41.15";
    public static final String DEFAULT_LON = "-8.61024";

    private WeatherService() { }

    // Forecast for default location
    public List<String> getWeather(Context context) throws IOException, JSONException {

        String lang = context.getResources().getConfiguration().getLocales().get(0).getLanguage();

        URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key="
                + API_Key + "&q=" + DEFAULT_LAT + "," + DEFAULT_LON + "&days=" + "14" + "&lang=" + lang);

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        JSONObject json = new JSONObject(response);
        if (!json.has("forecast")) {
            throw new IOException("WeatherAPI error: " + response);
        }

        JSONArray forecastDays = json.getJSONObject("forecast").getJSONArray("forecastday");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < forecastDays.length(); i++) {
            JSONObject d = forecastDays.getJSONObject(i);
            JSONObject condition = d.getJSONObject("day").getJSONObject("condition");
            result.add(d.getString("date") + ";"          // day, e.g. 2026-11-08
                    + condition.getInt("code") + ";"      // code, e.g. 1183
                    + condition.getString("text"));       // description in the chosen language
        }
        return result;
    }

    // Forecast for GPS location
    public List<String> getWeather(Context context, String LAT, String LON) throws IOException, JSONException {

        String lang = context.getResources().getConfiguration().getLocales().get(0).getLanguage();

        URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key="
                    + API_Key + "&q=" + LAT + "," + LON + "&days=" + "14" + "&lang=" + lang);

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
            ? httpConn.getInputStream()
            : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        JSONObject json = new JSONObject(response);
        if (!json.has("forecast")) {
            throw new IOException("WeatherAPI error: " + response);
        }

        JSONArray forecastDays = json.getJSONObject("forecast").getJSONArray("forecastday");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < forecastDays.length(); i++) {
            JSONObject d = forecastDays.getJSONObject(i);
            JSONObject condition = d.getJSONObject("day").getJSONObject("condition");
            result.add(d.getString("date") + ";"          // day, e.g. 2026-11-08
                    + condition.getInt("code") + ";"      // code, e.g. 1183
                    + condition.getString("text"));       // description in the chosen language
        }
        return result;
    }

    private static String toDescription(int code) {
        switch (code) {
            // sunny
            case 1000:
                return "Sunny";
            // partly cloudy
            case 1003:
                return "Partly Cloudy";
            // cloudy
            case 1006: case 1009:
                return "Cloudy";
            // fog
            case 1135: case 1147:
                return "Fog";
            // rain
            case 1063: case 1072: case 1150: case 1153: case 1168:
            case 1180: case 1183: case 1186: case 1189: case 1198:
            case 1204: case 1207: case 1240: case 1249: case 1252:
            case 1261: case 1264:
                return "Rain";
            // heavy rain
            case 1171: case 1192: case 1195: case 1201: case 1237:
            case 1243: case 1246:
                return "Heavy Rain";
            // snow
            case 1066: case 1114: case 1117: case 1210: case 1213:
            case 1216: case 1219: case 1222: case 1225: case 1255:
            case 1258:
                return "Snow";
            // thunder
            case 1087: case 1273: case 1276: case 1279: case 1282:
                return "Thunder";
            default:
                return "Código Errado";
        }
    }
}