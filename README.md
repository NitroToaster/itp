# FridgeApp
[Open repository in Eclipse Che](https://che.stud.ntnu.no/#https://git.ntnu.no/IT1901-2025-groups/gr2536)
## App Description
FridgeApp is an application that allows users to keep track of which ingredients and food items they have at all times. Users update it by adding and removing items as they are purchased or used. Based on the available ingredients, the application can suggest recipes.  

It will also be possible to search for and browse recipes, as well as create shopping lists either manually or based on existing recipes. After shopping, users can easily add all purchased items to the app, and similarly remove ingredients when cooking recipes.

## Project Structure

### docs
The [**docs**](docs/) folder contains documentation related to each submission/milestone of the project. This includes project plans, progress reports, diagrams, and other relevant documentation.

### fridgeapp
The **fridgeapp** folder contains all the source code. The project is divided into multiple modules: `core`, `fxui` and `persistence`.

#### Modules

##### core
- **Fridge/core** – Class for creating and managing a fridge.    
- **Item/core** – Class representing items in the fridge.  
- **ItemFilters/core** - Class for filtering Items based on SearchCriteria.
- **ItemList/core** - List interface implemented by Fridge and ShoppingList
- **NameMatchMode/core** - Different ways Item can match user input.
- **SearchCriteria/core** - Class with a structured object for holding search parameters.
- **SearchSort/core** - Class that sorts based on search.
- **ShoppingList/core** - Class containing logic for ShoppingList functionality.

##### fxui
- **FridgeApp/fxui** – The JavaFX application providing the Fridge user interface.  
- **FridgeAppController/fxui** – Controller class for the JavaFX application.
- **FridgeService/fxui** - Service class for information exchange between Fridge and ShoppingList
- **ShoppingList/fxui** - Controller class for ShoppingList UI
- **resources/gr2536/fxui** - Contains FridgeApp.css, FridgeApp.fxml and ShoppingList.fxml


#### Utils
- **FridgeJsonFileManager** - Writes and reads to file


##### More Information
See [fxui/README.md](fridgeapp/fxui/README.md) for details on the user interface and functionality.

## Technologies and Dependencies
- **Java version:** 17 or higher  
- **Maven version:** 3.8.x or higher  
- **JavaFX:** 17.0.12  

## Releases
- V1
- [V2](docs/release2/README.md)

## How to Run the Project
```bash
cd fridgeapp
mvn -f fxui/pom.xml clean javafx:run  
