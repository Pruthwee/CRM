package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfTest {

    private Pdf pdf;

    @BeforeEach
    void setUp() {
        pdf = new Pdf();
    }

    @Test
    void testPdfBuilder() {
        Pdf built = Pdf.builder()
                .id(1L)
                .name("test.pdf")
                .content("Test PDF Content")
                .build();

        assertNotNull(built);
        assertEquals(1L, built.getId());
        assertEquals("test.pdf", built.getName());
        assertEquals("Test PDF Content", built.getContent());
    }

    @Test
    void testAllArgsConstructor() {
        Pdf pdf = new Pdf(1L, "test.pdf", "Test Content");
        assertNotNull(pdf);
        assertEquals(1L, pdf.getId());
        assertEquals("test.pdf", pdf.getName());
        assertEquals("Test Content", pdf.getContent());
    }

    @Test
    void testNoArgsConstructor() {
        Pdf pdf = new Pdf();
        assertNotNull(pdf);
    }

    @Test
    void testGettersAndSetters() {
        pdf.setId(1L);
        pdf.setName("document.pdf");
        pdf.setContent("Sample content");

        assertEquals(1L, pdf.getId());
        assertEquals("document.pdf", pdf.getName());
        assertEquals("Sample content", pdf.getContent());
    }

    @Test
    void testSetName() {
        pdf.setName("report.pdf");
        assertEquals("report.pdf", pdf.getName());
    }

    @Test
    void testSetContent() {
        pdf.setContent("This is a test PDF");
        assertEquals("This is a test PDF", pdf.getContent());
    }

    @Test
    void testNullName() {
        pdf.setName(null);
        assertNull(pdf.getName());
    }

    @Test
    void testNullContent() {
        pdf.setContent(null);
        assertNull(pdf.getContent());
    }

    @Test
    void testPdfEntity() {
        assertTrue(pdf.getClass().isAnnotationPresent(jakarta.persistence.Entity.class));
    }

    @Test
    void testPdfHasIdAnnotation() throws NoSuchFieldException {
        assertTrue(Pdf.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.Id.class));
    }

    @Test
    void testPdfHasGeneratedValueAnnotation() throws NoSuchFieldException {
        assertTrue(Pdf.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.GeneratedValue.class));
    }

    @Test
    void testContentIsTransient() throws NoSuchFieldException {
        assertTrue(Pdf.class.getDeclaredField("content").isAnnotationPresent(jakarta.persistence.Transient.class));
    }

    @Test
    void testNameMinSize() {
        pdf.setName("ab");
        assertEquals(2, pdf.getName().length());
    }
}
