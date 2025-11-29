package crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void testStatusEnumValues() {
        assertNotNull(Status.PROPOSED);
        assertNotNull(Status.NEGOTIATED);
        assertNotNull(Status.IMPLEMENTED);
        assertNotNull(Status.DONE);
    }

    @Test
    void testStatusIsEnum() {
        assertTrue(Status.class.isEnum());
    }

    @Test
    void testStatusAllArray() {
        assertNotNull(Status.ALL);
        assertEquals(4, Status.ALL.length);
        assertEquals(Status.PROPOSED, Status.ALL[0]);
        assertEquals(Status.NEGOTIATED, Status.ALL[1]);
        assertEquals(Status.IMPLEMENTED, Status.ALL[2]);
        assertEquals(Status.DONE, Status.ALL[3]);
    }

    @Test
    void testStatusValueOf() {
        assertEquals(Status.PROPOSED, Status.valueOf("PROPOSED"));
        assertEquals(Status.NEGOTIATED, Status.valueOf("NEGOTIATED"));
        assertEquals(Status.IMPLEMENTED, Status.valueOf("IMPLEMENTED"));
        assertEquals(Status.DONE, Status.valueOf("DONE"));
    }

    @Test
    void testStatusValues() {
        Status[] values = Status.values();
        assertEquals(4, values.length);
    }

    @Test
    void testStatusName() {
        assertEquals("PROPOSED", Status.PROPOSED.name());
        assertEquals("NEGOTIATED", Status.NEGOTIATED.name());
        assertEquals("IMPLEMENTED", Status.IMPLEMENTED.name());
        assertEquals("DONE", Status.DONE.name());
    }

    @Test
    void testStatusOrdinal() {
        assertEquals(0, Status.PROPOSED.ordinal());
        assertEquals(1, Status.NEGOTIATED.ordinal());
        assertEquals(2, Status.IMPLEMENTED.ordinal());
        assertEquals(3, Status.DONE.ordinal());
    }

    @Test
    void testStatusComparison() {
        assertTrue(Status.PROPOSED.ordinal() < Status.NEGOTIATED.ordinal());
        assertTrue(Status.NEGOTIATED.ordinal() < Status.IMPLEMENTED.ordinal());
        assertTrue(Status.IMPLEMENTED.ordinal() < Status.DONE.ordinal());
    }

    @Test
    void testInvalidStatusValueOf() {
        assertThrows(IllegalArgumentException.class, () -> {
            Status.valueOf("INVALID");
        });
    }
}
