import * as FridgeService from "./FridgeService";
import api from "./api";

jest.mock("./api");

describe("FridgeService", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("fetchItems", () => {
    it("should fetch items successfully", async () => {
      const mockData = [
        { id: 1, name: "Milk", qty: 2, expiration: "2025-10-30" }
      ];
      api.get.mockResolvedValue({ data: mockData });

      const result = await FridgeService.fetchItems();

      expect(api.get).toHaveBeenCalledWith("/fridge/items");
      expect(result).toEqual(mockData);
    });

    it("should handle fetch error", async () => {
      api.get.mockRejectedValue(new Error("Network error"));

      await expect(FridgeService.fetchItems()).rejects.toThrow("Network error");
    });
  });

  describe("addItemApi", () => {
    it("should add item successfully", async () => {
      const newItem = { name: "Cheese", qty: 1, expiration: "2025-11-01" };
      const mockResponse = { id: 2, ...newItem };
      api.post.mockResolvedValue({ data: mockResponse });

      const result = await FridgeService.addItemApi(newItem);

      expect(api.post).toHaveBeenCalledWith("/fridge/items", newItem);
      expect(result).toEqual(mockResponse);
    });
  });

  describe("removeItemApi", () => {
    it("should remove item successfully", async () => {
      api.delete.mockResolvedValue({ data: { success: true } });

      const result = await FridgeService.removeItemApi(1);

      expect(api.delete).toHaveBeenCalledWith("/fridge/items/1");
      expect(result).toEqual({ success: true });
    });
  });

  describe("updateItemApi", () => {
    it("should update item successfully", async () => {
      const updatedItem = { id: 1, name: "Updated Milk", qty: 3 };
      api.put.mockResolvedValue({ data: updatedItem });

      const result = await FridgeService.updateItemApi(1, updatedItem);

      expect(api.put).toHaveBeenCalledWith("/fridge/items/1", updatedItem);
      expect(result).toEqual(updatedItem);
    });
  });
});