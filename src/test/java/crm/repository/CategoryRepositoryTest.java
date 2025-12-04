package crm.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryRepositoryTest {

    @Test
    public void testCategoryRepositoryInterfaceExists() {
        assertNotNull(CategoryRepository.class);
    }

    @Test
    public void testFindByNameMethodExists() throws Exception {
        assertNotNull(CategoryRepository.class.getDeclaredMethod("findByName", String.class));
    }
}
