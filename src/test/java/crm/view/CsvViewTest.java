package crm.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CsvViewTest {

    @Test
    public void testCsvViewConstructor() {
        CsvView csvView = new CsvView();
        assertNotNull(csvView);
    }

    @Test
    public void testCsvViewContentType() {
        CsvView csvView = new CsvView();
        assertEquals("text/csv", csvView.getContentType());
    }
}
