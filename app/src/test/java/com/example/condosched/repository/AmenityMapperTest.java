package com.example.condosched.repository;

import com.example.condosched.domain.Amenity;
import com.example.condosched.dto.AmenityDTO;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class AmenityMapperTest {
    @Test
    public void convertsTransportDataIntoDomainData() {
        AmenityDTO dto = new Gson().fromJson("{\"id\":1,\"buildingId\":7,\"name\":\"Piscina\","
                + "\"openingTime\":\"08:30:00\",\"closingTime\":\"20:00:00\","
                + "\"bookingFee\":2.5,\"rules\":\"Sem mergulhos\",\"isActive\":true}", AmenityDTO.class);
        Amenity amenity = AmenityMapper.toDomain(dto, "Building A");
        assertEquals(1, amenity.getId());
        assertEquals("Building A", amenity.getBuildingName());
        assertEquals(LocalTime.of(8, 30), amenity.getOpeningTime());
        assertEquals(LocalTime.of(20, 0), amenity.getClosingTime());
        assertEquals(2.5, amenity.getBookingFee(), 0.001);
        assertEquals("Sem mergulhos", amenity.getRules());
        assertTrue(amenity.isActive());
        amenity.setName("Outro nome");
        assertEquals("Piscina", dto.getName());
    }

    @Test
    public void missingOrInvalidTimesRemainUnknown() {
        AmenityDTO dto = new AmenityDTO();
        dto.setOpeningTime("25:80:00");
        Amenity amenity = AmenityMapper.toDomain(dto, null);
        assertNull(amenity.getOpeningTime());
        assertNull(amenity.getClosingTime());
        assertNull(amenity.getBookingFee());
    }

    @Test
    public void realMidnightAndFreeFeeAreNotTreatedAsMissing() {
        AmenityDTO dto = new AmenityDTO();
        dto.setOpeningTime("00:00:00");
        dto.setBookingFee(0.0);
        Amenity amenity = AmenityMapper.toDomain(dto, "Building A");
        assertEquals(LocalTime.MIDNIGHT, amenity.getOpeningTime());
        assertEquals(0, amenity.getBookingFee(), 0);
    }

    @Test
    public void domainObjectCanBeTransportedToDetailsWithItsTypedTime() throws Exception {
        AmenityDTO dto = new AmenityDTO();
        dto.setOpeningTime("08:30:00");
        dto.setRules("Sem mergulhos");
        Amenity amenity = AmenityMapper.toDomain(dto, "Building A");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(amenity);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            Amenity restored = (Amenity) input.readObject();
            assertEquals(LocalTime.of(8, 30), restored.getOpeningTime());
            assertEquals("Building A", restored.getBuildingName());
            assertEquals("Sem mergulhos", restored.getRules());
        }
    }
}
