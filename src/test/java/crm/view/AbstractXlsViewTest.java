package crm.view;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbstractXlsViewTest {

    private class TestAbstractXlsView extends AbstractXlsView {
        @Override
        protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        }
    }

    @Test
    public void testAbstractXlsViewConstructor() {
        TestAbstractXlsView view = new TestAbstractXlsView();
        assertNotNull(view);
    }

    @Test
    public void testAbstractXlsViewContentType() {
        TestAbstractXlsView view = new TestAbstractXlsView();
        assertEquals("application/vnd.ms-excel", view.getContentType());
    }

    @Test
    public void testGeneratesDownloadContent() {
        TestAbstractXlsView view = new TestAbstractXlsView();
        assertTrue(view.generatesDownloadContent());
    }

    @Test
    public void testCreateWorkbook() {
        TestAbstractXlsView view = new TestAbstractXlsView();
        Workbook workbook = view.createWorkbook();
        assertNotNull(workbook);
    }
}
