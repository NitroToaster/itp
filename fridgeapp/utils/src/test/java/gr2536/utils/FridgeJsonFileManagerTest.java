package gr2536.utils;

import java.io.File;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import gr2536.core.Fridge;
import gr2536.core.Item;

public class FridgeJsonFileManagerTest {

    private FridgeJsonFileManager fileManager;
    private static final String TEST_FILE = "testdata/fridge_test.json";

    @BeforeEach
    void setUp() {
        fileManager = new FridgeJsonFileManager();
    }

    @AfterEach
    void tearDown() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /** 
     * Sammenligner to kjøleskap for innholdslikhet.
     * Forutsetter at Item har korrekt equals() og hashCode().
     */
    private boolean checkEqualFridges(Fridge f1, Fridge f2) {
        if (f1.listItems().size() != f2.listItems().size()) {
            return false;
        }
        for (Item item : f1.listItems()) {
            if (!f2.listItems().contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Test
    void testSaveAndReadFridgeData() {
        Fridge original = new Fridge();
        Item milk = new Item("Milk", 1, LocalDate.now().plusDays(5));
        Item eggs = new Item("Eggs", 12, LocalDate.now().plusDays(8));
        original.add(milk);
        original.add(eggs);

        fileManager.saveFridgeData(original, TEST_FILE);
        Fridge loaded = fileManager.readFridgeData(TEST_FILE);

        Assertions.assertTrue(checkEqualFridges(original, loaded),
                "Fridge loaded from JSON should be identical to the original");
    }

    @Test
    void testReadNonExistentFile() {
        Fridge loaded = fileManager.readFridgeData("nonexistent.json");
        Assertions.assertTrue(loaded.listItems().isEmpty(), 
        "Fridge loaded from non-existent file should be empty");
    }

    @Test
    void testReadEmptyFile() throws Exception {
        File emptyFile = new File(TEST_FILE);
        emptyFile.getParentFile().mkdirs();
        emptyFile.createNewFile();

        Fridge loaded = fileManager.readFridgeData(TEST_FILE);
        Assertions.assertTrue(loaded.listItems().isEmpty(),
                "Fridge loaded from empty file should be empty");
    }

    @Test
    void testSaveToInvalidJson() throws Exception {
        File badFile = new File(TEST_FILE);
        badFile.getParentFile().mkdirs();
        java.nio.file.Files.writeString(badFile.toPath(), "Not a JSON content");

        Assertions.assertThrows(RuntimeException.class, () -> {
            fileManager.readFridgeData(TEST_FILE);
        }, "Reading from a file with invalid JSON should throw RuntimeException");
    }

    @Test
    void testCreatesParentDirectoriesWhenSaving() {
        String nestedFile = "testdata/nested/dir/fridge_test.json";
        Fridge fridge = new Fridge();
        fridge.add(new Item("Juice", 1, LocalDate.now().plusDays(3)));

        fileManager.saveFridgeData(fridge, nestedFile);

        File savedFile = new File(nestedFile);
        Assertions.assertTrue(savedFile.exists(), "File should be created in nested directories");

        savedFile.delete();
        savedFile.getParentFile().delete();
    }



}

