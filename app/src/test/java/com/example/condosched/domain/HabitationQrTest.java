package com.example.condosched.domain;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class HabitationQrTest {
    @Test public void readsUnitIdentifier() {
        assertEquals(11L, HabitationQr.parse("condosched://unit/11"));
    }

    @Test public void rejectsUnrelatedAndMalformedCodes() {
        String[] invalid = {null, "", "11", "https://example.com/unit/11",
                "condosched://unit/0", "condosched://unit/-1", "condosched://unit/11/extra",
                "condosched://unit/11?unit=12", "condosched://unit/999999999999999999999"};
        for (String value : invalid) {
            assertThrows(IllegalArgumentException.class, () -> HabitationQr.parse(value));
        }
    }
}
