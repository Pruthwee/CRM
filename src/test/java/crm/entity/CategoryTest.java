package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
    }

    @Test
    void testGettersAndSetters() {
        category.setId(1L);
        category.setName("Test Category");

        assertEquals(1L, category.getId());
        assertEquals("Test Category", category.getName());
    }

    @Test
    void testSetId() {
        category.setId(5L);
        assertEquals(5L, category.getId());
    }

    @Test
    void testSetName() {
        category.setName("Electronics");
        assertEquals("Electronics", category.getName());
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
    void testCategoryEntity() {
        assertNotNull(category);
        assertTrue(category.getClass().isAnnotationPresent(jakarta.persistence.Entity.class));
    }

    @Test
    void testCategoryHasIdAnnotation() throws NoSuchFieldException {
        assertTrue(Category.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.Id.class));
    }

    @Test
    void testCategoryHasGeneratedValueAnnotation() throws NoSuchFieldException {
        assertTrue(Category.class.getDeclaredField("id").isAnnotationPresent(jakarta.persistence.GeneratedValue.class));
    }

    @Test
    void testCategoryHasTableAnnotation() {
        assertTrue(Category.class.isAnnotationPresent(jakarta.persistence.Table.class));
    }

    @Test
    void testMultipleCategories() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Clothing");

        assertNotEquals(category1.getId(), category2.getId());
        assertNotEquals(category1.getName(), category2.getName());
    }
}
