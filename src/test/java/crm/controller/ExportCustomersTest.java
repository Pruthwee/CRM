package crm.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportCustomersTest {

    @Test
    void testDefaultConstructor() {
        ExportCustomers exportCustomers = new ExportCustomers();
        assertNotNull(exportCustomers);
    }

    @Test
    void testClassInstantiation() {
        ExportCustomers ec = new ExportCustomers();
        assertNotNull(ec);
        assertTrue(ec instanceof ExportCustomers);
    }
}
