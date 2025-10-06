package gr2536.core; 
public interface FridgeFileManager { 
    void saveFridgeData(Fridge fridge, String filename); 
    Fridge readFridgeData(String filename); 
}
