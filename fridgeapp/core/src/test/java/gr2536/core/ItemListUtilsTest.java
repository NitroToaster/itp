package gr2536.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ItemListUtilsTest {

    /*
     * Tests clampToIntMax method.
     */
    @Test
    public void clampToIntMaxTest() {
        assertEquals(42, ItemListUtils.clampToIntMax(42));
        assertEquals(Integer.MAX_VALUE, ItemListUtils.clampToIntMax((long) Integer.MAX_VALUE + 100));  
    }
    
}
