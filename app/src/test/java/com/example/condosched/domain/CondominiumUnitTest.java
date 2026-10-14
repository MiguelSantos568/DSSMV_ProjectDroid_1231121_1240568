package com.example.condosched.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CondominiumUnitTest {

    @Test
    public void coordToName_isCorrect() {
        Building tempBuild = new Building();
        Condominium testCondo = new Condominium(0, "condoName", tempBuild, 25.8, 62.1);
        testCondo.addCoordinatesToName();
        assertEquals("condoName;lat:25.8;lon:62.1", testCondo.getName());
    }
}