package crm.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelViewTest {

    @Test
    public void testExcelViewConstructor() {
        ExcelView excelView = new ExcelView();
        assertNotNull(excelView);
    }

    @Test
    public void testExcelViewContentType() {
        ExcelView excelView = new ExcelView();
        assertEquals("application/vnd.ms-excel", excelView.getContentType());
    }
}
