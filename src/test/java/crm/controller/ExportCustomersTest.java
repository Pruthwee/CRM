package crm.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExportCustomersTest {

    @Test
    public void testExportCustomersClassExists() {
        assertNotNull(ExportCustomers.class);
    }

    @Test
    public void testExportCustomersConstructor() {
        ExportCustomers exportCustomers = new ExportCustomers();
        assertNotNull(exportCustomers);
    }
}
