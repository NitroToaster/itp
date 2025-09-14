package gr2536.fxui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.service.query.NodeQuery;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Test suite for application fxui
 */
class FridgeAppTest extends FxuiTestBase {

    @Test
    @DisplayName("application should load stage")
    void should_load_stage(){
        WaitForAsyncUtils.waitForFxEvents();

        NodeQuery nq = lookup("#itemInput");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#addButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#removeButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#loadButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#saveButton");
        assertFalse(nq.queryAll().isEmpty());

        sleep(1000);
    }

    
}