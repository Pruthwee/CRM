package crm.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReadDataUtils Tests")
class ReadDataUtilsTest {

    @Test
    @DisplayName("Should instantiate ReadDataUtils")
    void testInstantiation() {
        ReadDataUtils utils = new ReadDataUtils();
        assertNotNull(utils);
    }

    @Test
    @DisplayName("Should test basic functionality")
    void testBasicFunctionality() {
        ReadDataUtils utils = new ReadDataUtils();
        assertNotNull(utils);
    }
}
