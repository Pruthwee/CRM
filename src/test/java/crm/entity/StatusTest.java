package crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatusTest {

    @Test
    public void testStatusEnumValues() {
        Status[] statuses = Status.values();
        assertEquals(4, statuses.length);
    }

    @Test
    public void testStatusProposed() {
        assertEquals(Status.PROPOSED, Status.valueOf("PROPOSED"));
    }

    @Test
    public void testStatusNegotiated() {
        assertEquals(Status.NEGOTIATED, Status.valueOf("NEGOTIATED"));
    }

    @Test
    public void testStatusImplemented() {
        assertEquals(Status.IMPLEMENTED, Status.valueOf("IMPLEMENTED"));
    }

    @Test
    public void testStatusDone() {
        assertEquals(Status.DONE, Status.valueOf("DONE"));
    }

    @Test
    public void testStatusAllArray() {
        assertNotNull(Status.ALL);
        assertEquals(4, Status.ALL.length);
    }

    @Test
    public void testStatusAllContainsAllValues() {
        assertEquals(Status.PROPOSED, Status.ALL[0]);
        assertEquals(Status.NEGOTIATED, Status.ALL[1]);
        assertEquals(Status.IMPLEMENTED, Status.ALL[2]);
        assertEquals(Status.DONE, Status.ALL[3]);
    }
}
