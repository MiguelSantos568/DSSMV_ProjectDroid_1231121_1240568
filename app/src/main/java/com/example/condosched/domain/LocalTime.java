package com.example.condosched.domain;

public class LocalTime {
    private int hour;
    private int minute;
    private int second;
    private int nano;

    public LocalTime() {}

    public LocalTime(int hour, int minute, int second, int nano) {
        if(hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }else{
            this.hour = hour;
        }

        if(minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }else{
            this.minute = minute;
        }

        if(second < 0 || second > 59) {
            throw new IllegalArgumentException("Second must be between 0 and 59");
        } else{
            this.second = second;
        }

        if(nano < 0 || nano > 1_000_000_000) {
            throw new IllegalArgumentException("Nano must be between 0 and 1,000,000,000");
        } else {
            this.nano = nano;
        }
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getNano() {
        return nano;
    }

    public void setNano(int nano) {
        this.nano = nano;
    }
}
