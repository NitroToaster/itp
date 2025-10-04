# FridgeApp
[Open repository in Eclipse Che](https://che.stud.ntnu.no/#https://git.ntnu.no/IT1901-2025-groups/gr2536)
## App Description
FridgeApp is an application that allows users to keep track of which ingredients and food items they have at all times. Users update it by adding and removing items as they are purchased or used. Based on the available ingredients, the application can suggest recipes.  

It will also be possible to search for and browse recipes, as well as create shopping lists either manually or based on existing recipes. After shopping, users can easily add all purchased items to the app, and similarly remove ingredients when cooking recipes.

## Project Structure

### docs
The [**docs**](docs/) folder contains documentation related to each submission/milestone of the project. This includes project plans, progress reports, diagrams, and other relevant documentation.

### fridgeapp
The **fridgeapp** folder contains all the source code. The project is divided into multiple modules: `core` and `fxui`.

#### Modules

##### core
- **Fridge/core** – Class for creating and managing a fridge.  
- **FridgeFileManager/core** – Class for saving and reading the fridge contents to/from a file.  
- **Item/core** – Class representing items in the fridge.  

##### fxui
- **FridgeApp/fxui** – The JavaFX application providing the user interface.  
- **FridgeAppController/fxui** – Controller classes for the JavaFX application.  

##### More Information
See [fxui/README.md](fridgeapp/fxui/README.md) for details on the user interface and functionality.

## Technologies and Dependencies
- **Java version:** 17 or higher  
- **Maven version:** 3.8.x or higher  
- **JavaFX:** 17.0.12  

## How to Run the Project
```bash
cd fridgeapp
mvn -f fxui/pom.xml clean javafx:run  
