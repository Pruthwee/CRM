package crm.repository;

import crm.entity.Category;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRepositoryTest {

    @Test
    public void testCustomerRepositoryInterfaceExists() {
        assertNotNull(CustomerRepository.class);
    }

    @Test
    public void testGetMaxIdMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("getMaxId"));
    }

    @Test
    public void testFindAllByEnabledMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findAllByEnabled", int.class));
    }

    @Test
    public void testFindOneByEnabledAndNameMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findOneByEnabledAndName", int.class, String.class));
    }

    @Test
    public void testFindOneByNameMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findOneByName", String.class));
    }

    @Test
    public void testFindByEnabledAndEmailMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByEnabledAndEmail", int.class, String.class));
    }

    @Test
    public void testFindByEmailMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByEmail", String.class));
    }

    @Test
    public void testFindByEnabledAndCityMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByEnabledAndCity", int.class, String.class));
    }

    @Test
    public void testFindByCityMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByCity", String.class));
    }

    @Test
    public void testFindByCityAndAddressMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByCityAndAddress", String.class, String.class));
    }

    @Test
    public void testFindByPhoneMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByPhone", int.class));
    }

    @Test
    public void testFindByFirstNameMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByFirstName", String.class));
    }

    @Test
    public void testFindByLastNameMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByLastName", String.class));
    }

    @Test
    public void testFindByFirstNameAndLastNameMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByFirstNameAndLastName", String.class, String.class));
    }

    @Test
    public void testFindByCategoriesMethodExists() throws Exception {
        assertNotNull(CustomerRepository.class.getDeclaredMethod("findByCategories", Set.class));
    }
}
