package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Status Enum Tests")
class StatusTest {

    @Test
    @DisplayName("Should have all expected status values")
    void testAllStatusValues() {
        Status[] statuses = Status.values();
        assertEquals(4, statuses.length);
        assertEquals(Status.PROPOSED, statuses[0]);
        assertEquals(Status.NEGOTIATED, statuses[1]);
        assertEquals(Status.IMPLEMENTED, statuses[2]);
        assertEquals(Status.DONE, statuses[3]);
    }

    @Test
    @DisplayName("Should have correct ALL constant array")
    void testAllConstantArray() {
        Status[] all = Status.ALL;
        assertEquals(4, all.length);
        assertEquals(Status.PROPOSED, all[0]);
        assertEquals(Status.NEGOTIATED, all[1]);
        assertEquals(Status.IMPLEMENTED, all[2]);
        assertEquals(Status.DONE, all[3]);
    }

    @Test
    @DisplayName("Should correctly valueOf from string")
    void testValueOf() {
        assertEquals(Status.PROPOSED, Status.valueOf("PROPOSED"));
        assertEquals(Status.NEGOTIATED, Status.valueOf("NEGOTIATED"));
        assertEquals(Status.IMPLEMENTED, Status.valueOf("IMPLEMENTED"));
        assertEquals(Status.DONE, Status.valueOf("DONE"));
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void testInvalidValueOf() {
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should test enum name method")
    void testEnumName() {
        assertEquals("PROPOSED", Status.PROPOSED.name());
        assertEquals("NEGOTIATED", Status.NEGOTIATED.name());
        assertEquals("IMPLEMENTED", Status.IMPLEMENTED.name());
        assertEquals("DONE", Status.DONE.name());
    }

    @Test
    @DisplayName("Should test enum ordinal")
    void testEnumOrdinal() {
        assertEquals(0, Status.PROPOSED.ordinal());
        assertEquals(1, Status.NEGOTIATED.ordinal());
        assertEquals(2, Status.IMPLEMENTED.ordinal());
        assertEquals(3, Status.DONE.ordinal());
    }

    @Test
    @DisplayName("Should test enum equality")
    void testEnumEquality() {
        Status status1 = Status.PROPOSED;
        Status status2 = Status.PROPOSED;
        assertEquals(status1, status2);
        assertSame(status1, status2);
    }

    @Test
    @DisplayName("Should test enum inequality")
    void testEnumInequality() {
        assertNotEquals(Status.PROPOSED, Status.DONE);
        assertNotEquals(Status.NEGOTIATED, Status.IMPLEMENTED);
    }

    @Test
    @DisplayName("Should test enum toString")
    void testEnumToString() {
        assertEquals("PROPOSED", Status.PROPOSED.toString());
        assertEquals("NEGOTIATED", Status.NEGOTIATED.toString());
        assertEquals("IMPLEMENTED", Status.IMPLEMENTED.toString());
        assertEquals("DONE", Status.DONE.toString());
    }

    @Test
    @DisplayName("Should test switch statement with all statuses")
    void testSwitchStatement() {
        for (Status status : Status.values()) {
            String result = switch (status) {
                case PROPOSED -> "proposed";
                case NEGOTIATED -> "negotiated";
                case IMPLEMENTED -> "implemented";
                case DONE -> "done";
            };
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Should test compareTo method")
    void testCompareTo() {
        assertTrue(Status.PROPOSED.compareTo(Status.DONE) < 0);
        assertTrue(Status.DONE.compareTo(Status.PROPOSED) > 0);
        assertEquals(0, Status.NEGOTIATED.compareTo(Status.NEGOTIATED));
    }

    @Test
    @DisplayName("Should test ALL array is immutable reference")
    void testAllArrayReference() {
        Status[] all1 = Status.ALL;
        Status[] all2 = Status.ALL;
        assertSame(all1, all2);
    }
}
