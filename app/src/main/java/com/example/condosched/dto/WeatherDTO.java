package com.example.condosched.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherDTO {
    private ForecastData forecast;

    public WeatherDTO() { }
    public ForecastData getForecast() { return forecast; }
    public void setForecast(ForecastData forecast) { this.forecast = forecast; }

    // A estrutura acompanha o JSON: forecast -> forecastday -> day -> condition.
    public static class ForecastData {
        @SerializedName("forecastday")
        private List<ForecastDay> days;

        public ForecastData() { }
        public List<ForecastDay> getDays() { return days; }
        public void setDays(List<ForecastDay> days) { this.days = days; }
    }

    public static class ForecastDay {
        private String date;
        private Day day;

        public ForecastDay() { }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Day getDay() { return day; }
        public void setDay(Day day) { this.day = day; }
    }

    public static class Day {
        private Condition condition;

        public Day() { }
        public Condition getCondition() { return condition; }
        public void setCondition(Condition condition) { this.condition = condition; }
    }

    public static class Condition {
        private Integer code;
        private String text;

        public Condition() { }
        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
