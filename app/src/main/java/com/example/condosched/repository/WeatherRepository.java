package com.example.condosched.repository;

import com.example.condosched.BuildConfig;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Forecast;
import com.example.condosched.dto.WeatherDTO;
import com.example.condosched.network.ApiClient;
import com.example.condosched.network.WeatherApi;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private final WeatherApi api;
    private final String key;
    private Call<WeatherDTO> request;
    private int version;

    public WeatherRepository() {
        this(ApiClient.weather(), BuildConfig.WEATHER_API_KEY);
    }

    WeatherRepository(WeatherApi api, String key) {
        this.api = api;
        this.key = key;
    }

    public void load(Double latitude, Double longitude, int days, String language,
                     RepositoryCallback<List<Forecast>> callback) {
        cancel();
        int current = version;
        if (key == null || key.trim().isEmpty()) {
            callback.onError(DataError.SERVICE_UNAVAILABLE);
            return;
        }
        String location = "41.15,-8.61024"; // Porto, quando não há localização disponível.
        if (latitude != null && longitude != null && Double.isFinite(latitude) && Double.isFinite(longitude)
                && Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180) {
            location = latitude + "," + longitude;
        }
        request = api.getForecast(key, location, days, language);
        request.enqueue(new Callback<WeatherDTO>() {
            @Override
            public void onResponse(Call<WeatherDTO> call, Response<WeatherDTO> response) {
                if (call.isCanceled() || current != version) return;
                if (!response.isSuccessful()) {
                    callback.onError(DataError.LOAD_FAILED);
                    return;
                }
                WeatherDTO dto = response.body();
                if (dto == null || dto.getForecast() == null || dto.getForecast().getDays() == null) {
                    callback.onError(DataError.INVALID_RESPONSE);
                    return;
                }
                List<Forecast> forecasts = new ArrayList<>();
                for (WeatherDTO.ForecastDay item : dto.getForecast().getDays()) {
                    if (item == null || item.getDate() == null || item.getDay() == null
                            || item.getDay().getCondition() == null) {
                        callback.onError(DataError.INVALID_RESPONSE);
                        return;
                    }
                    WeatherDTO.Condition condition = item.getDay().getCondition();
                    if (condition.getCode() == null || condition.getText() == null
                            || condition.getText().trim().isEmpty()) {
                        callback.onError(DataError.INVALID_RESPONSE);
                        return;
                    }
                    try {
                        forecasts.add(new Forecast(LocalDate.parse(item.getDate()),
                                condition.getCode(), condition.getText()));
                    } catch (DateTimeParseException error) {
                        callback.onError(DataError.INVALID_RESPONSE);
                        return;
                    }
                    if (forecasts.size() == days) break;
                }
                callback.onSuccess(forecasts);
            }

            @Override
            public void onFailure(Call<WeatherDTO> call, Throwable error) {
                if (!call.isCanceled() && current == version) callback.onError(DataError.LOAD_FAILED);
            }
        });
    }

    public void cancel() {
        version++;
        if (request != null) request.cancel();
    }
}
