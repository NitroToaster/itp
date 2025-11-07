import FridgeService from "./FridgeService";
import ShoppingListService from "./ShoppingListService";
import RecipeService from "./RecipeService";

// Mock axios
jest.mock("./api", () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

describe("API Services", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("FridgeService", () => {
    it("should be defined", () => {
      expect(FridgeService).toBeDefined();
    });

    it("should have required methods", () => {
      expect(typeof FridgeService.getFridgeItems).toBe("function");
      expect(typeof FridgeService.addFridgeItem).toBe("function");
      expect(typeof FridgeService.updateFridgeItem).toBe("function");
      expect(typeof FridgeService.deleteFridgeItem).toBe("function");
    });
  });

  describe("ShoppingListService", () => {
    it("should be defined", () => {
      expect(ShoppingListService).toBeDefined();
    });

    it("should have required methods", () => {
      expect(typeof ShoppingListService.getShoppingList).toBe("function");
      expect(typeof ShoppingListService.addShoppingItem).toBe("function");
      expect(typeof ShoppingListService.updateShoppingItem).toBe("function");
      expect(typeof ShoppingListService.deleteShoppingItem).toBe("function");
    });
  });

  describe("RecipeService", () => {
    it("should be defined", () => {
      expect(RecipeService).toBeDefined();
    });

    it("should have required methods", () => {
      expect(typeof RecipeService.getRecipes).toBe("function");
      expect(typeof RecipeService.searchRecipes).toBe("function");
    });
  });
});