package com.example.condosched.domain;

import java.time.LocalDate;

public final class Forecast {
    private final LocalDate date;
    private final int conditionCode;
    private final String description;

    public Forecast(LocalDate date, int conditionCode, String description) {
        this.date = date;
        this.conditionCode = conditionCode;
        this.description = description;
    }

    public LocalDate getDate() { return date; }
    public int getConditionCode() { return conditionCode; }
    public String getDescription() { return description; }
}
