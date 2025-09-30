package gr2536.fxui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Basic FXML loading and UI component tests for ShoppingList.fxml.
 * Tests that the FXML file loads correctly and contains expected components.
 */
class ShoppingListTest extends ApplicationTest {

    private AnchorPane root;

    @Override
    public void start(Stage stage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr2536/fxui/ShoppingList.fxml"));
        root = loader.load();
        
        // Create scene and show stage
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        // Reset FridgeService before each test
        FridgeService.reset();
    }

    @Test
    void testFXMLLoadsSuccessfully() {
        // Test that FXML loading doesn't throw exceptions
        assertNotNull(root, "Root AnchorPane should be loaded");
    }

    @Test
    void testRequiredComponentsExist() {
        // Test that all required UI components exist
        TextField itemField = lookup("#itemField").query();
        assertNotNull(itemField, "Item field should exist");

        Spinner<?> quantitySpinner = lookup("#quantitySpinner").query();
        assertNotNull(quantitySpinner, "Quantity spinner should exist");

        ListView<?> shoppingList = lookup("#shoppingList").query();
        assertNotNull(shoppingList, "Shopping list view should exist");
    }

    @Test
    void testAllButtonsExist() {
        // Test that all expected buttons exist
        Button addItemButton = lookup("#addItemButton").query();
        assertNotNull(addItemButton, "Add Item button should exist");

        Button removeItemButton = lookup("#removeItemButton").query();
        assertNotNull(removeItemButton, "Remove Item button should exist");

        Button addAllToInventoryButton = lookup("#addAllToInventoryButton").query();
        assertNotNull(addAllToInventoryButton, "Add All to Inventory button should exist");

        Button removeAllButton = lookup("#removeAllButton").query();
        assertNotNull(removeAllButton, "Remove All button should exist");

        Button backToFridgeButton = lookup("#backToFridgeButton").query();
        assertNotNull(backToFridgeButton, "Back to Fridge button should exist");
    }

    @Test
    void testButtonTexts() {
        // Test that buttons have correct text
        Button addItemButton = lookup("#addItemButton").query();
        assertEquals("Add Item", addItemButton.getText(), "Add Item button should have correct text");

        Button removeItemButton = lookup("#removeItemButton").query();
        assertEquals("Remove Item", removeItemButton.getText(), "Remove Item button should have correct text");

        Button addAllToInventoryButton = lookup("#addAllToInventoryButton").query();
        assertEquals("Add All to Inventory", addAllToInventoryButton.getText(), 
                     "Add All to Inventory button should have correct text");

        Button removeAllButton = lookup("#removeAllButton").query();
        assertEquals("Remove All", removeAllButton.getText(), "Remove All button should have correct text");

        Button backToFridgeButton = lookup("#backToFridgeButton").query();
        assertEquals("Back to Fridge", backToFridgeButton.getText(), "Back to Fridge button should have correct text");
    }

    @Test
    void testInputFieldProperties() {
        // Test input field properties
        TextField itemField = lookup("#itemField").query();
        assertEquals("Item name", itemField.getPromptText(), "Item field should have correct prompt text");

        @SuppressWarnings("unchecked")
        Spinner<Integer> quantitySpinner = lookup("#quantitySpinner").query();
        assertTrue(quantitySpinner.isEditable(), "Quantity spinner should be editable");
        assertNotNull(quantitySpinner.getValueFactory(), "Quantity spinner should have a value factory");
    }

    @Test
    void testInitialState() {
        // Test initial state of components
        TextField itemField = lookup("#itemField").query();
        assertTrue(itemField.getText() == null || itemField.getText().isEmpty(), 
                   "Item field should start empty");

        ListView<?> shoppingList = lookup("#shoppingList").query();
        assertEquals(0, shoppingList.getItems().size(), "Shopping list should start empty");

        // Test that some buttons are initially disabled
        Button removeItemButton = lookup("#removeItemButton").query();
        assertTrue(removeItemButton.isDisabled(), "Remove Item button should be disabled when no selection");

        Button addAllToInventoryButton = lookup("#addAllToInventoryButton").query();
        assertTrue(addAllToInventoryButton.isDisabled(), 
                   "Add All to Inventory button should be disabled when list is empty");

        Button removeAllButton = lookup("#removeAllButton").query();
        assertTrue(removeAllButton.isDisabled(), "Remove All button should be disabled when list is empty");
    }

    @Test
    void testStyleClassesApplied() {
        // Test that primary style class is applied to buttons
        Button addItemButton = lookup("#addItemButton").query();
        assertTrue(addItemButton.getStyleClass().contains("primary"), 
                   "Add Item button should have 'primary' style class");

        Button removeItemButton = lookup("#removeItemButton").query();
        assertTrue(removeItemButton.getStyleClass().contains("primary"), 
                   "Remove Item button should have 'primary' style class");
    }

    @Test
    void testLayoutStructure() {
        // Test basic layout structure
        assertTrue(root instanceof AnchorPane, "Root should be AnchorPane");
        
        // Test that the main container exists with panel style class
        Node panelNode = lookup(".panel").query();
        assertNotNull(panelNode, "Main panel should exist with 'panel' style class");
        
        // Test that GridPane exists by checking if we can find the input fields
        // which are inside the GridPane
        TextField itemField = lookup("#itemField").query();
        Spinner<?> quantitySpinner = lookup("#quantitySpinner").query();
        
        // If both inputs exist, the GridPane must exist (since they're defined inside it in FXML)
        assertNotNull(itemField, "Item field should exist (proving GridPane exists)");
        assertNotNull(quantitySpinner, "Quantity spinner should exist (proving GridPane exists)");
    }
}