import * as DataService from "./DataService";

describe("DataService", () => {
  let mockFileReader;

  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock FileReader
    mockFileReader = {
      readAsText: jest.fn(),
      onload: null,
      onerror: null,
      result: null
    };
    
    global.FileReader = jest.fn(() => mockFileReader);
  });

  describe("importData", () => {
    it("should import valid JSON data", async () => {
      const mockFile = new File(
        [JSON.stringify({ fridgeItems: [], shoppingItems: [] })],
        "data.json",
        { type: "application/json" }
      );

      const promise = DataService.importData(mockFile);
      
      // Simulate FileReader onload
      mockFileReader.result = JSON.stringify({ 
        fridgeItems: [{ id: 1, name: "Milk" }], 
        shoppingItems: [{ id: 1, name: "Bread" }] 
      });
      mockFileReader.onload({ target: mockFileReader });

      const result = await promise;

      expect(result).toEqual({
        fridgeItems: [{ id: 1, name: "Milk" }],
        shoppingItems: [{ id: 1, name: "Bread" }]
      });
    });

    it("should handle invalid JSON", async () => {
      const mockFile = new File(["invalid json"], "data.json", { type: "application/json" });

      const promise = DataService.importData(mockFile);
      
      mockFileReader.result = "invalid json";
      mockFileReader.onload({ target: mockFileReader });

      await expect(promise).rejects.toThrow();
    });

    it("should handle file read error", async () => {
      const mockFile = new File(["data"], "data.json", { type: "application/json" });

      const promise = DataService.importData(mockFile);
      
      const error = new Error("Read failed");
      mockFileReader.onerror(error);

      await expect(promise).rejects.toThrow("Read failed");
    });
  });

  describe("exportData", () => {
    it("should export data as JSON", () => {
      const data = {
        fridgeItems: [{ id: 1, name: "Milk" }],
        shoppingItems: [{ id: 1, name: "Bread" }]
      };

      // Mock URL and document methods
      global.URL.createObjectURL = jest.fn(() => "blob:mock-url");
      global.URL.revokeObjectURL = jest.fn();
      const mockClick = jest.fn();
      const mockLink = {
        href: "",
        download: "",
        click: mockClick,
        remove: jest.fn()
      };
      jest.spyOn(document, "createElement").mockReturnValue(mockLink);
      jest.spyOn(document.body, "appendChild").mockImplementation(() => {});

      DataService.exportData(data);

      expect(document.createElement).toHaveBeenCalledWith("a");
      expect(mockClick).toHaveBeenCalled();
    });
  });
});