package gr2536.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FridgeFileManagerTest {

    private FridgeFileManager fileManager;
    private Fridge fridge;
    
    @TempDir
    Path tempDir;

    /**
     * Sets up a filemanager and fridge for each test.
     */
    @BeforeEach
    void setUp() {
        fileManager = new FridgeFileManager();
        fridge = new Fridge();
    }

    /**
     * Tests saveFridgeData functionality.
     * @throws IOException if failed.
     */
    @Test
    void testSaveFridgeData() throws IOException {
        Item milk = new Item("Milk", 2, "Drink", LocalDate.of(2025, 9, 25));
        fridge.add(milk);
        
        Path testFile = tempDir.resolve("test_fridge.txt");
        fileManager.saveFridgeData(fridge, testFile.toString());
        
        assertTrue(Files.exists(testFile));
        List<String> lines = Files.readAllLines(testFile);
        assertEquals("Milk,2,Drink,2025-09-25", lines.get(0));
    }

    /**
     * Tests functionality of readFridgeData.
     * @throws IOException if failed.
     */
    @Test
    void testReadFridgeData() throws IOException {
        Path testFile = tempDir.resolve("test_read.txt");
        String content = "Milk,2,Drink,2025-09-25\n";
        Files.write(testFile, content.getBytes());
        
        fileManager.readFridgeData(fridge, testFile.toString());
        
        List<Item> items = fridge.listItems();
        assertEquals(1, items.size());
        assertEquals("Milk", items.get(0).getName());
        assertEquals(2, items.get(0).getQuantity());
    }

    /**
     * Tests both save and read functionality.
     * @throws IOException if failed.
     */
    @Test
    void testSaveAndReadRoundTrip() throws IOException {
        Item milk = new Item("Milk", 2, "Drink", LocalDate.of(2025, 9, 25));
        fridge.add(milk);
        
        Path testFile = tempDir.resolve("roundtrip.txt");
        fileManager.saveFridgeData(fridge, testFile.toString());
        
        Fridge newFridge = new Fridge();
        fileManager.readFridgeData(newFridge, testFile.toString());
        
        assertEquals(1, newFridge.listItems().size());
        assertEquals("Milk", newFridge.listItems().get(0).getName());
    }

    /**
     * Tests edge case where input file does not exist.
     */
    @Test
    void testReadNonExistentFile() {
        assertDoesNotThrow(() -> 
            fileManager.readFridgeData(fridge, "nonexistent.txt")
        );
        assertTrue(fridge.listItems().isEmpty());
    }
}
