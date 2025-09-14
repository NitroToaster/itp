package gr2536.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class FridgeFileManager {

    public void saveFridgeData(Fridge fridge, String filename) {

        List<Item> itemsInFridge = fridge.listItems();

        try {
            File file = new File(filename);

            PrintWriter writer = new PrintWriter(file);

            for (Item item : itemsInFridge) {
                writer.write(item.getName()+","+
                        item.getQuantity()+","+
                        item.getUnit()+","+
                        item.getExpirationDate()+"\n");
            }
            writer.flush();
            writer.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readFridgeData(Fridge fridge, String filename) {
        File file = new File(filename);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String name = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                String unit = parts[2];
                LocalDate expirationDate = LocalDate.parse(parts[3]);
                Item item = new Item(name, quantity, unit, expirationDate);
                fridge.add(item);
            }
            scanner.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();}
    }

    public static void main(String[] args) {
        Fridge fridge = new Fridge();
        FridgeFileManager fileManager = new FridgeFileManager();
        String filename = "fridge_data.txt";

        fileManager.readFridgeData(fridge, filename);

        System.out.println("Innhold i kjøleskapet ved oppstart:");
        fridge.listItems().forEach(System.out::println);
        
        Item milk = new Item("milk", 2, "liter", LocalDate.of(2024, 6, 30));
        Item eggs = new Item("eggs", 12, "pieces", LocalDate.of(2024, 7, 5));
        Item broccoli = new Item("broccoli", 1, "piece", LocalDate.of(2024, 6, 28));

        fridge.add(milk);
        fridge.add(eggs);
        fridge.add(broccoli);

        System.out.println("\nEtter at vi har lagt til nye varer:");
        fridge.listItems().forEach(System.out::println);

        fileManager.saveFridgeData(fridge, "fridge_data.txt");
        fileManager.readFridgeData(fridge, "fridge_data.txt");
    }
}
