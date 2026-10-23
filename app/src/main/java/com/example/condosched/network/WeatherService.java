package com.example.condosched.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherService{

    private static final String API_Key = "0334a61464474874a8f112148261410";
    private static String Location = "Oporto";
    private static String Days = "7";
    private static String Language = "pt";

    public static void getWeather(String[] args) throws IOException {
        URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key="
                + API_Key + "&q=" + Location + "&days=" + Days + "&lang=" + Language);
//        URL url = new URL("https://api.weatherapi.com/v1/forecast.json?key="
//                + API_Key + "&q=" + args[1] + "&days=" + args[2] + "&lang=" + Language);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
            ? httpConn.getInputStream()
            : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";

        // Extracting Desired Data From Response
    }
}