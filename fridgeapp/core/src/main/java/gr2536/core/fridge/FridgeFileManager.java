package gr2536.core.fridge;

public interface FridgeFileManager { 
    void saveFridgeData(Fridge fridge, String filename); 
    Fridge readFridgeData(String filename); 
}
