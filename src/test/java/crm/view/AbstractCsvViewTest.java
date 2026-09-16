package crm.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractCsvViewTest {

    // Concrete subclass for testing the abstract class
    private static class ConcreteCsvView extends AbstractCsvView {
        @Override
        protected void buildCsvDocument(
                java.util.Map<String, Object> model,
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response) throws Exception {
            // no-op for testing
        }
    }

    private ConcreteCsvView csvView;

    @BeforeEach
    void setUp() {
        csvView = new ConcreteCsvView();
    }

    @Test
    void testConstructor_setsContentType() {
        assertEquals("text/csv", csvView.getContentType());
    }

    @Test
    void testGeneratesDownloadContent_returnsTrue() {
        assertTrue(csvView.generatesDownloadContent());
    }

    @Test
    void testSetUrl_doesNotThrow() {
        assertDoesNotThrow(() -> csvView.setUrl("http://example.com/csv"));
    }

    @Test
    void testInstantiation_notNull() {
        assertNotNull(csvView);
    }

    @Test
    void testContentType_isCsv() {
        String contentType = csvView.getContentType();
        assertTrue(contentType.contains("csv"));
    }
}
