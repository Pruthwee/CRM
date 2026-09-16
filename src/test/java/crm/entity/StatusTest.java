package crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void testStatusValues() {
        Status[] values = Status.values();
        assertEquals(4, values.length);
    }

    @Test
    void testStatusProposed() {
        assertEquals(Status.PROPOSED, Status.valueOf("PROPOSED"));
    }

    @Test
    void testStatusNegotiated() {
        assertEquals(Status.NEGOTIATED, Status.valueOf("NEGOTIATED"));
    }

    @Test
    void testStatusImplemented() {
        assertEquals(Status.IMPLEMENTED, Status.valueOf("IMPLEMENTED"));
    }

    @Test
    void testStatusDone() {
        assertEquals(Status.DONE, Status.valueOf("DONE"));
    }

    @Test
    void testAllConstant() {
        Status[] all = Status.ALL;
        assertNotNull(all);
        assertEquals(4, all.length);
        assertEquals(Status.PROPOSED, all[0]);
        assertEquals(Status.NEGOTIATED, all[1]);
        assertEquals(Status.IMPLEMENTED, all[2]);
        assertEquals(Status.DONE, all[3]);
    }

    @Test
    void testStatusOrdinal() {
        assertEquals(0, Status.PROPOSED.ordinal());
        assertEquals(1, Status.NEGOTIATED.ordinal());
        assertEquals(2, Status.IMPLEMENTED.ordinal());
        assertEquals(3, Status.DONE.ordinal());
    }

    @Test
    void testStatusName() {
        assertEquals("PROPOSED", Status.PROPOSED.name());
        assertEquals("NEGOTIATED", Status.NEGOTIATED.name());
        assertEquals("IMPLEMENTED", Status.IMPLEMENTED.name());
        assertEquals("DONE", Status.DONE.name());
    }

    @Test
    void testInvalidStatusThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("INVALID"));
    }
}
