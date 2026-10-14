package com.example.condosched.domain.DTO;

import com.example.condosched.domain.LocalTime;

public class AmenityDTO {
    private long id;
    private String name;
    private String description;
    private String type;
    private long buildingId;
    private boolean requiresBooking;
    private int capacity;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private int bookingFee;
    private boolean isActive;
    private String rules;
}
