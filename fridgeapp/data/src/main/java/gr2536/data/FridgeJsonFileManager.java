package gr2536.data;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gr2536.core.fridge.Fridge;
import gr2536.core.fridge.FridgeFileManager;

public class FridgeJsonFileManager implements FridgeFileManager {

    private final ObjectMapper mapper;

    public FridgeJsonFileManager() {
        mapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    @Override
    public void saveFridgeData(Fridge fridge, String filename) {
    try {
        File file = new File(filename);
        
        // Opprett parent directories hvis de ikke eksisterer
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        
        mapper.writeValue(file, fridge);
    } catch (IOException e) {
        throw new RuntimeException("Failed to save fridge data to " + filename, e);
    }
}

    @Override
    public Fridge readFridgeData(String filename) {
        File file = new File(filename);
        
        // Hvis filen ikke eksisterer eller er tom, returner tom Fridge
        if (!file.exists() || file.length() == 0) {
            return new Fridge();
        }
        
        try {
            return mapper.readValue(file, Fridge.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read fridge data from " + filename, e);
        }
    }
}

    
