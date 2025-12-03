package crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void testEnumValues() {
        Status[] values = Status.values();
        assertEquals(4, values.length);
    }

    @Test
    void testProposed() {
        assertEquals(Status.PROPOSED, Status.valueOf("PROPOSED"));
    }

    @Test
    void testNegotiated() {
        assertEquals(Status.NEGOTIATED, Status.valueOf("NEGOTIATED"));
    }

    @Test
    void testImplemented() {
        assertEquals(Status.IMPLEMENTED, Status.valueOf("IMPLEMENTED"));
    }

    @Test
    void testDone() {
        assertEquals(Status.DONE, Status.valueOf("DONE"));
    }

    @Test
    void testAllConstant() {
        assertNotNull(Status.ALL);
        assertEquals(4, Status.ALL.length);
        assertEquals(Status.PROPOSED, Status.ALL[0]);
        assertEquals(Status.NEGOTIATED, Status.ALL[1]);
        assertEquals(Status.IMPLEMENTED, Status.ALL[2]);
        assertEquals(Status.DONE, Status.ALL[3]);
    }

    @Test
    void testValueOf() {
        assertEquals(Status.PROPOSED, Status.valueOf("PROPOSED"));
        assertEquals(Status.NEGOTIATED, Status.valueOf("NEGOTIATED"));
        assertEquals(Status.IMPLEMENTED, Status.valueOf("IMPLEMENTED"));
        assertEquals(Status.DONE, Status.valueOf("DONE"));
    }

    @Test
    void testInvalidValueOf() {
        assertThrows(IllegalArgumentException.class, () -> {
            Status.valueOf("INVALID");
        });
    }

    @Test
    void testNullValueOf() {
        assertThrows(NullPointerException.class, () -> {
            Status.valueOf(null);
        });
    }
}
