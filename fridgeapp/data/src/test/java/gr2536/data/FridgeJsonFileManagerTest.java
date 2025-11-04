package gr2536.data;

import java.io.File;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.io.IOException;

import gr2536.core.Fridge;
import gr2536.core.Item;
import gr2536.data.FridgeJsonFileManager;

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

    /*
     * Helper method to check if two fridges are equal in terms of their items.
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

    /*
     * Tests that saving and reading fridge data works correctly.
     */
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

    /*
     * Tests that reading from a non-existent file results in an empty Fridge.
     */
    @Test
    void testReadNonExistentFile() {
        Fridge loaded = fileManager.readFridgeData("nonexistent.json");
        Assertions.assertTrue(loaded.listItems().isEmpty(), 
        "Fridge loaded from non-existent file should be empty");
    }

    /*
     * Tests that reading from an empty file results in an empty Fridge.
     */
    @Test
    void testReadEmptyFile() throws Exception {
        File emptyFile = new File(TEST_FILE);
        emptyFile.getParentFile().mkdirs();
        emptyFile.createNewFile();

        Fridge loaded = fileManager.readFridgeData(TEST_FILE);
        Assertions.assertTrue(loaded.listItems().isEmpty(),
                "Fridge loaded from empty file should be empty");
    }

    /*
     * Tests that reading from a file with invalid JSON throws RuntimeException.
     */
    @Test
    void testSaveToInvalidJson() throws Exception {
        File badFile = new File(TEST_FILE);
        badFile.getParentFile().mkdirs();
        java.nio.file.Files.writeString(badFile.toPath(), "Not a JSON content");

        Assertions.assertThrows(RuntimeException.class, () -> {
            fileManager.readFridgeData(TEST_FILE);
        }, "Reading from a file with invalid JSON should throw RuntimeException");
    }

    /*
     * Tests that saveFridgeData creates parent directories when saving to a nested path.
     */
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

    /*
     * Tests that saveFridgeData creates parent directories when they are missing.
     */
    @Test
    void testCreatesParentDirectoriesWhenMissing() throws IOException {
    File nestedFile = new File("testdata/missing/dir/fridge_test.json");
    File parentDir = nestedFile.getParentFile();

    if (parentDir.exists()) {
        java.nio.file.Files.walk(parentDir.toPath())
            .sorted(java.util.Comparator.reverseOrder())
            .map(java.nio.file.Path::toFile)
            .forEach(File::delete);
    }

    Fridge fridge = new Fridge();
    fridge.add(new Item("Butter", 1, LocalDate.now().plusDays(2)));

    fileManager.saveFridgeData(fridge, nestedFile.getPath());

    Assertions.assertTrue(nestedFile.exists(), "File should be created when parent dirs are missing");

    java.nio.file.Files.walk(new File("testdata/missing").toPath())
        .sorted(java.util.Comparator.reverseOrder())
        .map(java.nio.file.Path::toFile)
        .forEach(File::delete);
    }

    /*
     * Tests that saveFridgeData works correctly when there is no parent directory (i.e., saving to the current directory).
     */
    @Test
    void testSaveFridgeDataWithNoParentDirectory() {
    String filename = "fridge_root_test.json";

    Fridge fridge = new Fridge();
    fridge.add(new Item("Cheese", 1, LocalDate.now().plusDays(4)));

    fileManager.saveFridgeData(fridge, filename);

    File savedFile = new File(filename);
    Assertions.assertTrue(savedFile.exists(), "File should be created in current directory even with no parent");

    savedFile.delete();
    }

    /*
     * Tests that saveFridgeData throws RuntimeException when an IOException occurs (e.g., when trying to write to a directory instead of a file).
     */
    @Test
    void testSaveFridgeDataThrowsRuntimeExceptionOnIoError() throws IOException {
    File directoryAsFile = new File("testdata/directoryAsFile");

    if (directoryAsFile.exists()) {
        java.nio.file.Files.walk(directoryAsFile.toPath())
            .sorted(java.util.Comparator.reverseOrder())
            .map(java.nio.file.Path::toFile)
            .forEach(File::delete);
    }

    directoryAsFile.getParentFile().mkdirs();
    java.nio.file.Files.writeString(directoryAsFile.toPath(), "I'm a file, not a directory!");

    Fridge fridge = new Fridge();
    fridge.add(new Item("Yogurt", 1, LocalDate.now().plusDays(3)));

    Assertions.assertThrows(RuntimeException.class, () -> {
        fileManager.saveFridgeData(fridge, "testdata/directoryAsFile/fridge_test.json");
    }, "Should throw RuntimeException when saveFridgeData encounters IOException");

    directoryAsFile.delete();
    }
}

