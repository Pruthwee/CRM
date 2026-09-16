package crm.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import static org.junit.jupiter.api.Assertions.*;

class ExcelViewTest {

    private ExcelView excelView;

    @BeforeEach
    void setUp() {
        excelView = new ExcelView();
    }

    @Test
    void testDefaultConstructor() {
        ExcelView view = new ExcelView();
        assertNotNull(view);
    }

    @Test
    void testIsInstanceOfAbstractXlsView() {
        assertTrue(excelView instanceof AbstractXlsView);
    }

    @Test
    void testInstantiation_notNull() {
        assertNotNull(excelView);
    }
}
