package gr2536.springboot.config;

import gr2536.core.fridge.Fridge;
import gr2536.core.shoppingList.ShoppingList;
import gr2536.data.FridgeJsonFileManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
@EnableConfigurationProperties(FridgeProperties.class)
public class FridgeBeans {

  @Bean
  public FridgeJsonFileManager fridgeFileManager() {
    return new FridgeJsonFileManager();
  }

  @Bean
  public Fridge fridge(FridgeJsonFileManager fileManager, FridgeProperties props) {
    String path = props.getFile(); 

    File file = new File(path);
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      if (!parent.mkdirs() && !parent.exists()) {
        throw new IllegalStateException("Could not create directory: " + parent.getAbsolutePath());
      }
    }

    if (!file.exists()) {
      try {
        if (!file.createNewFile() && !file.exists()) {
          throw new IllegalStateException("Could not create fridge file: " + file.getAbsolutePath());
        }
      } catch (IOException e) {
        throw new IllegalStateException("Failed to create fridge file: " + file.getAbsolutePath(), e);
      }
    }

    Fridge loaded = fileManager.readFridgeData(path);
    loaded.setFileManager(fileManager, path);
    return loaded;
  }

  @Bean
  public ShoppingList shoppingList() {
    return new ShoppingList(); 
  }
}

/**
 * Optional, bindable properties for the fridge.
 * If 'fridge.file' isn't provided, we default to './data/fridge.json'.
 */
@ConfigurationProperties(prefix = "fridge")
class FridgeProperties {

  /**
   * Path to the JSON file used for persistence.
   * Defaults to ./data/fridge.json when not set.
   */
  private String file = "./data/fridge.json";

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }
}
