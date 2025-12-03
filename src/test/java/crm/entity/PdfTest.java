package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfTest {

    private Pdf pdf;

    @BeforeEach
    void setUp() {
        pdf = Pdf.builder()
                .id(1L)
                .name("test.pdf")
                .content("Test content")
                .build();
    }

    @Test
    void testConstructor() {
        assertNotNull(pdf);
    }

    @Test
    void testBuilder() {
        Pdf built = Pdf.builder()
                .name("builder.pdf")
                .content("Builder content")
                .build();

        assertNotNull(built);
        assertEquals("builder.pdf", built.getName());
    }

    @Test
    void testId() {
        assertEquals(1L, pdf.getId());
        pdf.setId(2L);
        assertEquals(2L, pdf.getId());
    }

    @Test
    void testName() {
        assertEquals("test.pdf", pdf.getName());
        pdf.setName("updated.pdf");
        assertEquals("updated.pdf", pdf.getName());
    }

    @Test
    void testContent() {
        assertEquals("Test content", pdf.getContent());
        pdf.setContent("Updated content");
        assertEquals("Updated content", pdf.getContent());
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
    void testEmptyName() {
        pdf.setName("");
        assertEquals("", pdf.getName());
    }

    @Test
    void testNoArgsConstructor() {
        Pdf empty = new Pdf();
        assertNotNull(empty);
    }

    @Test
    void testAllArgsConstructor() {
        Pdf full = new Pdf(1L, "name.pdf", "content");
        assertNotNull(full);
        assertEquals("name.pdf", full.getName());
        assertEquals("content", full.getContent());
    }
}
