package crm.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvViewTest {

    private CsvView csvView;

    @BeforeEach
    void setUp() {
        csvView = new CsvView();
    }

    @Test
    void testDefaultConstructor() {
        CsvView view = new CsvView();
        assertNotNull(view);
    }

    @Test
    void testContentType_isCsv() {
        assertEquals("text/csv", csvView.getContentType());
    }

    @Test
    void testGeneratesDownloadContent_returnsTrue() {
        assertTrue(csvView.generatesDownloadContent());
    }

    @Test
    void testIsInstanceOfAbstractCsvView() {
        assertTrue(csvView instanceof AbstractCsvView);
    }
}
