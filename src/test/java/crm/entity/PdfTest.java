package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pdf Entity Tests")
class PdfTest {

    private Pdf pdf;

    @BeforeEach
    void setUp() {
        pdf = new Pdf();
    }

    @Test
    @DisplayName("Should create pdf with default constructor")
    void testDefaultConstructor() {
        assertNotNull(pdf);
        assertNull(pdf.getId());
        assertNull(pdf.getName());
        assertNull(pdf.getContent());
    }

    @Test
    @DisplayName("Should create pdf with builder")
    void testBuilderConstructor() {
        Pdf builtPdf = Pdf.builder()
                .id(1L)
                .name("Test Document")
                .content("PDF content here")
                .build();

        assertNotNull(builtPdf);
        assertEquals(1L, builtPdf.getId());
        assertEquals("Test Document", builtPdf.getName());
        assertEquals("PDF content here", builtPdf.getContent());
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testSettersAndGetters() {
        pdf.setId(2L);
        pdf.setName("Contract Document");
        pdf.setContent("This is the PDF content");

        assertEquals(2L, pdf.getId());
        assertEquals("Contract Document", pdf.getName());
        assertEquals("This is the PDF content", pdf.getContent());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        pdf.setId(null);
        pdf.setName(null);
        pdf.setContent(null);

        assertNull(pdf.getId());
        assertNull(pdf.getName());
        assertNull(pdf.getContent());
    }

    @Test
    @DisplayName("Should handle empty name")
    void testEmptyName() {
        pdf.setName("");
        assertEquals("", pdf.getName());
    }

    @Test
    @DisplayName("Should handle empty content")
    void testEmptyContent() {
        pdf.setContent("");
        assertEquals("", pdf.getContent());
    }

    @Test
    @DisplayName("Should handle long name")
    void testLongName() {
        String longName = "A".repeat(1000);
        pdf.setName(longName);
        assertEquals(longName, pdf.getName());
    }

    @Test
    @DisplayName("Should handle long content")
    void testLongContent() {
        String longContent = "Content ".repeat(10000);
        pdf.setContent(longContent);
        assertEquals(longContent, pdf.getContent());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        Pdf pdf1 = Pdf.builder()
                .id(1L)
                .name("Same Doc")
                .content("Same content")
                .build();

        Pdf pdf2 = Pdf.builder()
                .id(1L)
                .name("Same Doc")
                .content("Same content")
                .build();

        assertEquals(pdf1, pdf2);
        assertEquals(pdf1.hashCode(), pdf2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        pdf.setId(1L);
        pdf.setName("Test PDF");
        String result = pdf.toString();
        assertNotNull(result);
        assertTrue(result.contains("Pdf"));
    }

    @Test
    @DisplayName("Should handle all args constructor")
    void testAllArgsConstructor() {
        Pdf newPdf = new Pdf(3L, "Full Document", "Full content here");

        assertEquals(3L, newPdf.getId());
        assertEquals("Full Document", newPdf.getName());
        assertEquals("Full content here", newPdf.getContent());
    }

    @Test
    @DisplayName("Should handle no args constructor")
    void testNoArgsConstructor() {
        Pdf newPdf = new Pdf();
        assertNotNull(newPdf);
        assertNull(newPdf.getId());
        assertNull(newPdf.getName());
        assertNull(newPdf.getContent());
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        String specialName = "Document-2024_@#$%.pdf";
        pdf.setName(specialName);
        assertEquals(specialName, pdf.getName());
    }

    @Test
    @DisplayName("Should handle special characters in content")
    void testSpecialCharactersInContent() {
        String specialContent = "Content with special chars: © ® ™ € £ ¥";
        pdf.setContent(specialContent);
        assertEquals(specialContent, pdf.getContent());
    }

    @Test
    @DisplayName("Should handle multiline content")
    void testMultilineContent() {
        String multilineContent = "Line 1\nLine 2\nLine 3";
        pdf.setContent(multilineContent);
        assertEquals(multilineContent, pdf.getContent());
    }
}
