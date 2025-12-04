package crm.service;

import crm.entity.Category;
import crm.entity.Customer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerServiceTest {

    @Test
    public void testCustomerServiceInterfaceExists() {
        assertNotNull(CustomerService.class);
    }

    @Test
    public void testGetMaxIdMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("getMaxId"));
    }

    @Test
    public void testListAllCustomersMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("listAllCustomers"));
    }

    @Test
    public void testShowCustomerMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("showCustomer", Long.class));
    }

    @Test
    public void testFindAllByEnabledTrueMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findAllByEnabledTrue"));
    }

    @Test
    public void testFindAllByEnabledFalseMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findAllByEnabledFalse"));
    }

    @Test
    public void testFindOneByEnabledTrueAndNameMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findOneByEnabledTrueAndName", String.class));
    }

    @Test
    public void testFindOneByEnabledFalseAndNameMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findOneByEnabledFalseAndName", String.class));
    }

    @Test
    public void testFindOneByNameMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findOneByName", String.class));
    }

    @Test
    public void testFindByEmailMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByEmail", String.class));
    }

    @Test
    public void testFindByPhoneMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByPhone", int.class));
    }

    @Test
    public void testFindByCategoriesMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByCategories", Set.class));
    }

    @Test
    public void testFindByFirstNameMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByFirstName", String.class));
    }

    @Test
    public void testFindByLastNameMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByLastName", String.class));
    }

    @Test
    public void testFindByFirstNameAndLastNameMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByFirstNameAndLastName", String.class, String.class));
    }

    @Test
    public void testFindByCityMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByCity", String.class));
    }

    @Test
    public void testFindByCityAndAddressMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("findByCityAndAddress", String.class, String.class));
    }

    @Test
    public void testSaveCustomerMethodExists() throws Exception {
        assertNotNull(CustomerService.class.getDeclaredMethod("saveCustomer", Customer.class));
    }
}
