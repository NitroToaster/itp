package gr2536.fxui;

import gr2536.core.fridge.Fridge;

/**
 * Shared service to manage the Fridge instance across different UI controllers.
 * This allows multiple controllers to work with the same Fridge data.
 */
public class FridgeService {
    
    private static Fridge fridge = new Fridge();
    
    /**
     * Get the shared Fridge instance.
     * @return the current Fridge instance
     */
    public static Fridge getFridge() {
        return fridge;
    }
    
    /**
     * Set a new Fridge instance (useful for loading from file).
     * @param newFridge the new Fridge to use
     */
    public static void setFridge(Fridge newFridge) {
        if (newFridge == null) {
            throw new IllegalArgumentException("Fridge cannot be null");
        }
        fridge = newFridge;
    }
    
    /**
     * Reset the fridge to an empty state.
     */
    public static void reset() {
        fridge = new Fridge();
    }
}
