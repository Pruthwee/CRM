package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerServiceTest {

    @Test
    public void testCustomerServiceInterface() {
        assertNotNull(CustomerService.class);
        assertTrue(CustomerService.class.isInterface());
    }

    @Test
    public void testGetMaxIdMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("getMaxId"));
    }

    @Test
    public void testListAllCustomersMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("listAllCustomers"));
    }

    @Test
    public void testShowCustomerMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("showCustomer", Long.class));
    }

    @Test
    public void testFindAllByEnabledTrueMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findAllByEnabledTrue"));
    }

    @Test
    public void testFindAllByEnabledFalseMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findAllByEnabledFalse"));
    }

    @Test
    public void testFindOneByEnabledTrueAndNameMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findOneByEnabledTrueAndName", String.class));
    }

    @Test
    public void testFindByEnabledTrueAndEmailMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findByEnabledTrueAndEmail", String.class));
    }

    @Test
    public void testFindByEnabledTrueAndPhoneMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findByEnabledTrueAndPhone", int.class));
    }

    @Test
    public void testSaveCustomerMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("saveCustomer", crm.entity.Customer.class));
    }

    @Test
    public void testFindByEnabledTrueAndCityMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findByEnabledTrueAndCity", String.class));
    }
}
