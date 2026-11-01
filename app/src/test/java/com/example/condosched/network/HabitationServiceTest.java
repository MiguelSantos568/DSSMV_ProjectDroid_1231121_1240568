package com.example.condosched.network;

import com.example.condosched.domain.Habitation;
import com.example.condosched.domain.DTO.CondominiumDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class HabitationServiceTest {
    @Test public void mapsNestedApiResponseWithDecimalValues() {
        String json = "[{\"id\":1,\"name\":\"Condo A\",\"buildings\":["
                + "{\"id\":1,\"name\":\"Building A\",\"units\":["
                + "{\"id\":11,\"unitNumber\":\"111\",\"floor\":2,"
                + "\"area\":85.5,\"monthlyFee\":120.75,\"permillage\":1.5}]}]}]";
        List<CondominiumDTO> condos = new Gson().fromJson(json,
                new TypeToken<List<CondominiumDTO>>() {}.getType());
        Habitation home = HabitationService.findIn(condos, 11);
        assertNotNull(home);
        assertEquals("Condo A", home.condominium);
        assertEquals("Building A", home.building);
        assertEquals("2", home.floor);
        assertEquals("111", home.unit);
        assertNull(HabitationService.findIn(condos, 111));
    }

    @Test public void skipsMissingHierarchyWithoutInventingAHome() {
        List<CondominiumDTO> condos = new Gson().fromJson("[null,{},"
                + "{\"buildings\":[null,{}, {\"units\":[null]}]}]",
                new TypeToken<List<CondominiumDTO>>() {}.getType());
        assertNull(HabitationService.findIn(condos, 1));
    }
}
