package gr2536.fxui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

        NodeQuery nq = lookup("#nameField");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#quantitySpinner");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#expirationPicker");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#addButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#addOneButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#removeOneButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#removeAllButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#loadButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#saveButton");
        assertFalse(nq.queryAll().isEmpty());

        nq = lookup("#fridgeList");
        assertFalse(nq.queryAll().isEmpty());

        sleep(1000);
    }

    
}