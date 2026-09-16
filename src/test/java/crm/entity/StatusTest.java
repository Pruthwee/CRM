package crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusTest {

    @Test
    void testProposedValue() {
        assertEquals(Status.PROPOSED, Status.valueOf("PROPOSED"));
    }

    @Test
    void testNegotiatedValue() {
        assertEquals(Status.NEGOTIATED, Status.valueOf("NEGOTIATED"));
    }

    @Test
    void testImplementedValue() {
        assertEquals(Status.IMPLEMENTED, Status.valueOf("IMPLEMENTED"));
    }

    @Test
    void testDoneValue() {
        assertEquals(Status.DONE, Status.valueOf("DONE"));
    }

    @Test
    void testAllArrayLength() {
        assertEquals(4, Status.ALL.length);
    }

    @Test
    void testAllArrayContainsProposed() {
        boolean found = false;
        for (Status s : Status.ALL) {
            if (s == Status.PROPOSED) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testAllArrayContainsNegotiated() {
        boolean found = false;
        for (Status s : Status.ALL) {
            if (s == Status.NEGOTIATED) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testAllArrayContainsImplemented() {
        boolean found = false;
        for (Status s : Status.ALL) {
            if (s == Status.IMPLEMENTED) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testAllArrayContainsDone() {
        boolean found = false;
        for (Status s : Status.ALL) {
            if (s == Status.DONE) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testValuesLength() {
        assertEquals(4, Status.values().length);
    }

    @Test
    void testOrdinalProposed() {
        assertEquals(0, Status.PROPOSED.ordinal());
    }

    @Test
    void testOrdinalNegotiated() {
        assertEquals(1, Status.NEGOTIATED.ordinal());
    }

    @Test
    void testOrdinalImplemented() {
        assertEquals(2, Status.IMPLEMENTED.ordinal());
    }

    @Test
    void testOrdinalDone() {
        assertEquals(3, Status.DONE.ordinal());
    }

    @Test
    void testNameProposed() {
        assertEquals("PROPOSED", Status.PROPOSED.name());
    }

    @Test
    void testNameDone() {
        assertEquals("DONE", Status.DONE.name());
    }

    @Test
    void testInvalidValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("INVALID"));
    }
}
