package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Premium");
    }

    @Test
    void testConstructor() {
        Category newCategory = new Category();
        assertNotNull(newCategory);
    }

    @Test
    void testId() {
        assertEquals(1L, category.getId());
        category.setId(2L);
        assertEquals(2L, category.getId());
    }

    @Test
    void testName() {
        assertEquals("Premium", category.getName());
        category.setName("Standard");
        assertEquals("Standard", category.getName());
    }

    @Test
    void testNullName() {
        category.setName(null);
        assertNull(category.getName());
    }

    @Test
    void testEmptyName() {
        category.setName("");
        assertEquals("", category.getName());
    }

    @Test
    void testGettersAndSetters() {
        category.setId(10L);
        category.setName("Enterprise");

        assertEquals(10L, category.getId());
        assertEquals("Enterprise", category.getName());
    }
}
