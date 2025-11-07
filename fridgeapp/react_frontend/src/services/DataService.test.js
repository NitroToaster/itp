import DataService from "./DataService";

describe("DataService", () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe("saveData", () => {
    it("should save data to localStorage", () => {
      const testData = { test: "data" };
      DataService.saveData("testKey", testData);
      const saved = localStorage.getItem("testKey");
      expect(saved).toBe(JSON.stringify(testData));
    });

    it("should handle empty data", () => {
      DataService.saveData("testKey", null);
      const saved = localStorage.getItem("testKey");
      expect(saved).toBe("null");
    });
  });

  describe("loadData", () => {
    it("should load data from localStorage", () => {
      const testData = { test: "data" };
      localStorage.setItem("testKey", JSON.stringify(testData));
      const loaded = DataService.loadData("testKey");
      expect(loaded).toEqual(testData);
    });

    it("should return null for non-existent key", () => {
      const loaded = DataService.loadData("nonExistentKey");
      expect(loaded).toBeNull();
    });

    it("should handle invalid JSON", () => {
      localStorage.setItem("testKey", "invalid json");
      const loaded = DataService.loadData("testKey");
      expect(loaded).toBeNull();
    });
  });

  describe("clearData", () => {
    it("should clear specific key from localStorage", () => {
      localStorage.setItem("testKey", "testValue");
      DataService.clearData("testKey");
      expect(localStorage.getItem("testKey")).toBeNull();
    });
  });
});