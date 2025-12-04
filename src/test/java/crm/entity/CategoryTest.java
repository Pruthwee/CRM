package crm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {

    private Category category;

    @BeforeEach
    public void setUp() {
        category = new Category();
    }

    @Test
    public void testCategoryConstructor() {
        assertNotNull(category);
    }

    @Test
    public void testSetAndGetId() {
        category.setId(1L);
        assertEquals(1L, category.getId());
    }

    @Test
    public void testSetAndGetName() {
        category.setName("Test Category");
        assertEquals("Test Category", category.getName());
    }

    @Test
    public void testCategoryWithNullName() {
        category.setName(null);
        assertNull(category.getName());
    }

    @Test
    public void testCategoryWithEmptyName() {
        category.setName("");
        assertEquals("", category.getName());
    }

    @Test
    public void testCategoryWithNullId() {
        category.setId(null);
        assertNull(category.getId());
    }
}
