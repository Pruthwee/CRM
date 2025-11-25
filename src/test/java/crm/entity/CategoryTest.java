package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {

    private Category category;

    @BeforeEach
    public void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Technology");
    }

    @Test
    public void testCategoryConstructor() {
        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Technology", category.getName());
    }

    @Test
    public void testCategoryDefaultConstructor() {
        Category emptyCategory = new Category();
        assertNotNull(emptyCategory);
    }

    @Test
    public void testSettersAndGetters() {
        category.setId(2L);
        assertEquals(2L, category.getId());

        category.setName("Healthcare");
        assertEquals("Healthcare", category.getName());
    }

    @Test
    public void testCategoryWithNullValues() {
        Category nullCategory = new Category();
        nullCategory.setId(null);
        nullCategory.setName(null);

        assertNull(nullCategory.getId());
        assertNull(nullCategory.getName());
    }

    @Test
    public void testCategoryIdUpdate() {
        category.setId(10L);
        assertEquals(10L, category.getId());
    }

    @Test
    public void testCategoryNameUpdate() {
        category.setName("Finance");
        assertEquals("Finance", category.getName());
    }

    @Test
    public void testCategoryEquality() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Technology");

        Category category2 = new Category();
        category2.setId(1L);
        category2.setName("Technology");

        assertEquals(category1, category2);
    }

    @Test
    public void testCategoryToString() {
        String categoryString = category.toString();
        assertNotNull(categoryString);
        assertTrue(categoryString.contains("Technology"));
    }

    @Test
    public void testCategoryNameNotEmpty() {
        category.setName("ValidName");
        assertFalse(category.getName().isEmpty());
    }
}
