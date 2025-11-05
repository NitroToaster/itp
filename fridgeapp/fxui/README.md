# FridgeApp - JavaFx UI

### [Home](/README.md)

## Application Description
FridgeApp is a user-friendly and practical application that helps you keep full control over the items in your fridge. With a simple and intuitive interface, you can quickly register food items, specify unit, quantity, and expiration date, so you always know what you have and what should be used first, helping to reduce food waste.

Future updates plan to include advanced features to make cooking even easier. This will include the ability for the app to analyze recipes and automatically check which ingredients you already have and which are missing, making it easier to plan meals and shop smarter.

## Screenshots of our application
![New pop-up add window in fridge](/docs/release2/screenshots/screenshot1.png)

![Fridge](/docs/release2/screenshots/screenshot2.png)

![Shopping List](/docs/release2/screenshots/screenshot3.png)


## Running & Configuration

- Backend (Spring Boot):
  - `RECIPES_MEALDB_BASE_URL` (default: `https://www.themealdb.com/api/json/v1/1`)
  - Start: `cd fridgeapp/springboot && mvn spring-boot:run`

- FX UI:
  - `RECIPES_API_BASE_URL` (default: `http://localhost:8080/api/v1/recipes`)
  - `RECIPES_API_DIRECT` (optional, `1` to use MealDB directly for debugging)
  - Start: `cd fridgeapp && mvn -pl fxui -am javafx:run`

## Recipes Filters

- Filters pane provides:
  - Ingredients (comma-separated)
  - Match: All | Any
  - Min matched (minimum ingredient overlaps)
  - Category and Area (from backend `/meta`)
  - Name search

- "Use my fridge":
  - Finds recipes that include at least one ingredient you have.
  - Cards show a badge: `have/total` (ingredients you have out of recipe ingredients).

If category/area alone are used, leave Ingredients empty and click Apply. Ensure backend is running.

