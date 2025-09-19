package gr2536.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * A class to manage reading from and writing to a file for the fridge inventory.
 */

public class FridgeFileManager {

    /**
     * Saves the contents of the fridge to a file.
     * @param fridge
     * @param filename
     */
    public void saveFridgeData(Fridge fridge, String filename) {

        List<Item> itemsInFridge = fridge.listItems();

        try {
            File file = new File(filename);

            PrintWriter writer = new PrintWriter(file);

            for (Item item : itemsInFridge) {
                writer.write(item.getName()+","+
                        item.getQuantity()+","+
                        item.getExpirationDate()+"\n");
            }
            writer.flush();
            writer.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the contents of the fridge from a file and adds them to the fridge.
     * @param fridge
     * @param filename
     */
    public void readFridgeData(Fridge fridge, String filename) {
        File file = new File(filename);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String name = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                LocalDate expirationDate = LocalDate.parse(parts[2]);
                Item item = new Item(name, quantity, expirationDate);
                fridge.add(item);
            }
            scanner.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();}
    }

    /**
     * A simple demonstration of reading from and writing to a file to test the functionality.
     */

    public static void main(String[] args) {
        Fridge fridge = new Fridge();
        FridgeFileManager fileManager = new FridgeFileManager();
        String filename = "fridge_data.txt";

        fileManager.readFridgeData(fridge, filename);

        System.out.println("Innhold i kjøleskapet ved oppstart:");
        fridge.listItems().forEach(System.out::println);
        
        Item milk = new Item("milk", 2, LocalDate.of(2024, 6, 30));
        Item eggs = new Item("eggs", 12, LocalDate.of(2024, 7, 5));
        Item broccoli = new Item("broccoli", 1, LocalDate.of(2024, 6, 28));

        fridge.add(milk);
        fridge.add(eggs);
        fridge.add(broccoli);

        System.out.println("\nEtter at vi har lagt til nye varer:");
        fridge.listItems().forEach(System.out::println);

        fileManager.saveFridgeData(fridge, "fridge_data.txt");
        fileManager.readFridgeData(fridge, "fridge_data.txt");
    }
}
