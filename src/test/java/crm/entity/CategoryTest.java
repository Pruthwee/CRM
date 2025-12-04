package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Category Entity Tests")
class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
    }

    @Test
    @DisplayName("Should create category with default constructor")
    void testDefaultConstructor() {
        assertNotNull(category);
        assertNull(category.getId());
        assertNull(category.getName());
    }

    @Test
    @DisplayName("Should set and get id correctly")
    void testSetAndGetId() {
        Long expectedId = 1L;
        category.setId(expectedId);
        assertEquals(expectedId, category.getId());
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void testSetAndGetName() {
        String expectedName = "Premium";
        category.setName(expectedName);
        assertEquals(expectedName, category.getName());
    }

    @Test
    @DisplayName("Should handle null id")
    void testNullId() {
        category.setId(null);
        assertNull(category.getId());
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        category.setName(null);
        assertNull(category.getName());
    }

    @Test
    @DisplayName("Should handle empty name")
    void testEmptyName() {
        String emptyName = "";
        category.setName(emptyName);
        assertEquals(emptyName, category.getName());
    }

    @Test
    @DisplayName("Should handle long name")
    void testLongName() {
        String longName = "A".repeat(1000);
        category.setName(longName);
        assertEquals(longName, category.getName());
    }

    @Test
    @DisplayName("Should test equals and hashCode consistency")
    void testEqualsAndHashCode() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Premium");

        Category category2 = new Category();
        category2.setId(1L);
        category2.setName("Premium");

        assertEquals(category1, category2);
        assertEquals(category1.hashCode(), category2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        category.setId(1L);
        category.setName("Standard");
        String result = category.toString();
        assertNotNull(result);
        assertTrue(result.contains("Category"));
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        String specialName = "Category-@#$%&*()";
        category.setName(specialName);
        assertEquals(specialName, category.getName());
    }
}
