package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class PdfTest {

    private Pdf pdf;

    @BeforeEach
    public void setUp() {
        pdf = new Pdf();
    }

    @Test
    public void testPdfConstructor() {
        assertNotNull(pdf);
    }

    @Test
    public void testPdfBuilderPattern() {
        Pdf builtPdf = Pdf.builder()
                .id(1L)
                .name("Test PDF")
                .content("Test Content")
                .build();
        assertNotNull(builtPdf);
        assertEquals("Test PDF", builtPdf.getName());
        assertEquals("Test Content", builtPdf.getContent());
    }

    @Test
    public void testAllArgsConstructor() {
        Pdf fullPdf = new Pdf(1L, "Test", "Content");
        assertNotNull(fullPdf);
        assertEquals("Test", fullPdf.getName());
    }

    @Test
    public void testSetAndGetId() {
        pdf.setId(1L);
        assertEquals(1L, pdf.getId());
    }

    @Test
    public void testSetAndGetName() {
        pdf.setName("Test PDF");
        assertEquals("Test PDF", pdf.getName());
    }

    @Test
    public void testSetAndGetContent() {
        pdf.setContent("Test Content");
        assertEquals("Test Content", pdf.getContent());
    }

    @Test
    public void testPdfWithNullName() {
        pdf.setName(null);
        assertNull(pdf.getName());
    }

    @Test
    public void testPdfWithNullContent() {
        pdf.setContent(null);
        assertNull(pdf.getContent());
    }

    @Test
    public void testPdfWithEmptyName() {
        pdf.setName("");
        assertEquals("", pdf.getName());
    }
}
