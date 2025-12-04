package crm.repository;

import crm.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryRepository Tests")
class CategoryRepositoryTest {

    @Mock
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Premium");
    }

    @Test
    @DisplayName("Should find category by name")
    void testFindByName() {
        when(categoryRepository.findByName("Premium")).thenReturn(category);

        Category result = categoryRepository.findByName("Premium");

        assertNotNull(result);
        assertEquals("Premium", result.getName());
        verify(categoryRepository, times(1)).findByName("Premium");
    }

    @Test
    @DisplayName("Should return null when category not found by name")
    void testFindByNameNotFound() {
        when(categoryRepository.findByName("NonExistent")).thenReturn(null);

        Category result = categoryRepository.findByName("NonExistent");

        assertNull(result);
        verify(categoryRepository, times(1)).findByName("NonExistent");
    }

    @Test
    @DisplayName("Should save category")
    void testSave() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryRepository.save(category);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Premium", result.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Should find category by id")
    void testFindById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Optional<Category> result = categoryRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Premium", result.get().getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when category not found by id")
    void testFindByIdNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Category> result = categoryRepository.findById(999L);

        assertFalse(result.isPresent());
        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find all categories")
    void testFindAll() {
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Standard");

        List<Category> categories = Arrays.asList(category, category2);
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> result = categoryRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete category")
    void testDelete() {
        doNothing().when(categoryRepository).delete(category);

        categoryRepository.delete(category);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    @DisplayName("Should delete category by id")
    void testDeleteById() {
        doNothing().when(categoryRepository).deleteById(1L);

        categoryRepository.deleteById(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should check if category exists by id")
    void testExistsById() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        boolean result = categoryRepository.existsById(1L);

        assertTrue(result);
        verify(categoryRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should return false when category does not exist")
    void testExistsByIdFalse() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        boolean result = categoryRepository.existsById(999L);

        assertFalse(result);
        verify(categoryRepository, times(1)).existsById(999L);
    }

    @Test
    @DisplayName("Should count categories")
    void testCount() {
        when(categoryRepository.count()).thenReturn(5L);

        long result = categoryRepository.count();

        assertEquals(5L, result);
        verify(categoryRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should handle null name in findByName")
    void testFindByNameNull() {
        when(categoryRepository.findByName(null)).thenReturn(null);

        Category result = categoryRepository.findByName(null);

        assertNull(result);
        verify(categoryRepository, times(1)).findByName(null);
    }

    @Test
    @DisplayName("Should handle empty name in findByName")
    void testFindByNameEmpty() {
        when(categoryRepository.findByName("")).thenReturn(null);

        Category result = categoryRepository.findByName("");

        assertNull(result);
        verify(categoryRepository, times(1)).findByName("");
    }

    @Test
    @DisplayName("Should delete all categories")
    void testDeleteAll() {
        doNothing().when(categoryRepository).deleteAll();

        categoryRepository.deleteAll();

        verify(categoryRepository, times(1)).deleteAll();
    }
}
