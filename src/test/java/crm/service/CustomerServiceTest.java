package crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(CustomerService.class);
    }

    @Test
    void testIsInterface() {
        assertTrue(CustomerService.class.isInterface());
    }

    @Test
    void testGetMaxIdMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("getMaxId"));
    }

    @Test
    void testListAllCustomersMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("listAllCustomers"));
    }

    @Test
    void testShowCustomerMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("showCustomer", Long.class));
    }

    @Test
    void testSaveCustomerMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("saveCustomer", crm.entity.Customer.class));
    }
}
