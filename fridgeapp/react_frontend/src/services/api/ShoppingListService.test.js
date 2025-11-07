import * as ShoppingListService from "./ShoppingListService";
import api from "./api";

jest.mock("./api");

describe("ShoppingListService", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("fetchShoppingItems", () => {
    it("should fetch shopping items successfully", async () => {
      const mockData = [
        { id: 1, name: "Bread", qty: 2 }
      ];
      api.get.mockResolvedValue({ data: mockData });

      const result = await ShoppingListService.fetchShoppingItems();

      expect(api.get).toHaveBeenCalledWith("/shopping-list/items");
      expect(result).toEqual(mockData);
    });

    it("should handle fetch error", async () => {
      api.get.mockRejectedValue(new Error("Network error"));

      await expect(ShoppingListService.fetchShoppingItems()).rejects.toThrow("Network error");
    });
  });

  describe("addShoppingItem", () => {
    it("should add shopping item successfully", async () => {
      const newItem = { name: "Eggs", qty: 12 };
      const mockResponse = { id: 2, ...newItem };
      api.post.mockResolvedValue({ data: mockResponse });

      const result = await ShoppingListService.addShoppingItem(newItem);

      expect(api.post).toHaveBeenCalledWith("/shopping-list/items", newItem);
      expect(result).toEqual(mockResponse);
    });
  });

  describe("removeShoppingItem", () => {
    it("should remove shopping item successfully", async () => {
      api.delete.mockResolvedValue({ data: { success: true } });

      const result = await ShoppingListService.removeShoppingItem(1);

      expect(api.delete).toHaveBeenCalledWith("/shopping-list/items/1");
      expect(result).toEqual({ success: true });
    });
  });

  describe("markBought", () => {
    it("should mark item as bought successfully", async () => {
      api.put.mockResolvedValue({ data: true });

      const result = await ShoppingListService.markBought(1);

      expect(api.put).toHaveBeenCalledWith("/shopping-list/items/1/bought");
      expect(result).toBe(true);
    });
  });
});