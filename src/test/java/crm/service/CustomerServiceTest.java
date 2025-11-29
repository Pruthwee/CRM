package crm.service;

import crm.entity.Category;
import crm.entity.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Test
    void testCustomerServiceInterface() {
        assertNotNull(CustomerService.class);
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
    void testFindAllByEnabledTrueMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findAllByEnabledTrue"));
    }

    @Test
    void testFindAllByEnabledFalseMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findAllByEnabledFalse"));
    }

    @Test
    void testFindOneByEnabledTrueAndNameMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findOneByEnabledTrueAndName", String.class));
    }

    @Test
    void testFindOneByEnabledFalseAndNameMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findOneByEnabledFalseAndName", String.class));
    }

    @Test
    void testFindOneByNameMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findOneByName", String.class));
    }

    @Test
    void testFindByEnabledTrueAndEmailMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findByEnabledTrueAndEmail", String.class));
    }

    @Test
    void testFindByEnabledFalseAndEmailMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findByEnabledFalseAndEmail", String.class));
    }

    @Test
    void testFindByEmailMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findByEmail", String.class));
    }

    @Test
    void testFindByEnabledTrueAndPhoneMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("findByEnabledTrueAndPhone", int.class));
    }

    @Test
    void testSaveCustomerMethodExists() throws NoSuchMethodException {
        assertNotNull(CustomerService.class.getMethod("saveCustomer", Customer.class));
    }

    @Test
    void testInterfaceIsPublic() {
        int modifiers = CustomerService.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
    }
}
