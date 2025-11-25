package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PdfTest {

    private Pdf pdf;

    @BeforeEach
    public void setUp() {
        pdf = Pdf.builder()
                .id(1L)
                .name("TestDocument")
                .content("Sample PDF content")
                .build();
    }

    @Test
    public void testPdfBuilder() {
        assertNotNull(pdf);
        assertEquals(1L, pdf.getId());
        assertEquals("TestDocument", pdf.getName());
        assertEquals("Sample PDF content", pdf.getContent());
    }

    @Test
    public void testPdfNoArgsConstructor() {
        Pdf emptyPdf = new Pdf();
        assertNotNull(emptyPdf);
    }

    @Test
    public void testPdfAllArgsConstructor() {
        Pdf newPdf = new Pdf(2L, "NewDocument", "New content");
        assertNotNull(newPdf);
        assertEquals(2L, newPdf.getId());
        assertEquals("NewDocument", newPdf.getName());
        assertEquals("New content", newPdf.getContent());
    }

    @Test
    public void testSettersAndGetters() {
        pdf.setId(3L);
        assertEquals(3L, pdf.getId());

        pdf.setName("UpdatedDocument");
        assertEquals("UpdatedDocument", pdf.getName());

        pdf.setContent("Updated content");
        assertEquals("Updated content", pdf.getContent());
    }

    @Test
    public void testPdfWithNullValues() {
        Pdf nullPdf = new Pdf();
        nullPdf.setId(null);
        nullPdf.setName(null);
        nullPdf.setContent(null);

        assertNull(nullPdf.getId());
        assertNull(nullPdf.getName());
        assertNull(nullPdf.getContent());
    }

    @Test
    public void testPdfNameMinSize() {
        pdf.setName("AB");
        assertEquals(2, pdf.getName().length());
        assertTrue(pdf.getName().length() >= 2);
    }

    @Test
    public void testPdfContentTransient() {
        Pdf transientPdf = new Pdf();
        transientPdf.setContent("This content is transient");
        assertEquals("This content is transient", transientPdf.getContent());
    }

    @Test
    public void testPdfEquality() {
        Pdf pdf1 = Pdf.builder().id(1L).name("Doc").content("Content").build();
        Pdf pdf2 = Pdf.builder().id(1L).name("Doc").content("Content").build();
        assertEquals(pdf1, pdf2);
    }

    @Test
    public void testPdfToString() {
        String pdfString = pdf.toString();
        assertNotNull(pdfString);
        assertTrue(pdfString.contains("TestDocument"));
    }
}
