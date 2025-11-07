import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import App from "../App";
import * as FridgeService from "../services/api/FridgeService";
import * as ShoppingListService from "../services/api/ShoppingListService";

// Mock the services
jest.mock("../services/api/FridgeService");
jest.mock("../services/api/ShoppingListService");

// Mock the child components to simplify testing
jest.mock("../features/fridge/Fridge", () => {
  return function MockFridge(props) {
    return (
      <div data-testid="fridge-page">
        <button onClick={() => props.onAdd({ name: "Test", qty: 1 })}>
          Add Item
        </button>
        <button onClick={() => props.onRemove(1)}>Remove Item</button>
        <button onClick={() => props.onUpdate({ id: 1, name: "Updated" })}>
          Update Item
        </button>
      </div>
    );
  };
});

jest.mock("../features/shopping/ShoppingList", () => {
  return function MockShoppingList(props) {
    return (
      <div data-testid="shopping-page">
        <button onClick={() => props.onAdd({ name: "Shop Item", qty: 1 })}>
          Add Shopping Item
        </button>
        <button onClick={() => props.onRemove(1)}>Remove Shopping Item</button>
        <button onClick={() => props.onMarkBought(1)}>Mark Bought</button>
      </div>
    );
  };
});

jest.mock("../components/layout/AppShell", () => {
  return function MockAppShell({ children, onNavigate, onDataLoaded }) {
    return (
      <div data-testid="app-shell">
        <button onClick={() => onNavigate("fridge")}>Navigate to Fridge</button>
        <button onClick={() => onNavigate("shopping")}>Navigate to Shopping</button>
        <button onClick={onDataLoaded}>Load Data</button>
        {children}
      </div>
    );
  };
});

describe("App Component", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Default mock implementations
    FridgeService.fetchItems.mockResolvedValue([
      { id: 1, name: "Milk", qty: 2, expiration: "2025-10-30" }
    ]);
    FridgeService.addItemApi.mockResolvedValue({
      id: 2,
      name: "Test",
      qty: 1
    });
    FridgeService.removeItemApi.mockResolvedValue({ success: true });
    FridgeService.updateItemApi.mockResolvedValue({
      id: 1,
      name: "Updated",
      qty: 1
    });

    ShoppingListService.fetchShoppingItems.mockResolvedValue([
      { id: 1, name: "Bread", qty: 1 }
    ]);
    ShoppingListService.addShoppingItem.mockResolvedValue({
      id: 2,
      name: "Shop Item",
      qty: 1
    });
    ShoppingListService.removeShoppingItem.mockResolvedValue({ success: true });
    ShoppingListService.markBought.mockResolvedValue(true);
  });

  describe("Initialization", () => {
    it("should render the App component", async () => {
      render(<App />);
      
      await waitFor(() => {
        expect(screen.getByTestId("app-shell")).toBeInTheDocument();
      });
    });

    it("should load fridge items on mount", async () => {
      render(<App />);

      await waitFor(() => {
        expect(FridgeService.fetchItems).toHaveBeenCalledTimes(1);
      });
    });

    it("should load shopping items on mount", async () => {
      render(<App />);

      await waitFor(() => {
        expect(ShoppingListService.fetchShoppingItems).toHaveBeenCalledTimes(1);
      });
    });

    it("should display fridge page by default", async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });
    });
  });

  describe("Navigation", () => {
    it("should switch to shopping list view", async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const shoppingButton = screen.getByText("Navigate to Shopping");
      fireEvent.click(shoppingButton);

      await waitFor(() => {
        expect(screen.getByTestId("shopping-page")).toBeInTheDocument();
      });
    });

    it("should switch back to fridge view", async () => {
      render(<App />);

      // Navigate to shopping
      const shoppingButton = screen.getByText("Navigate to Shopping");
      fireEvent.click(shoppingButton);

      await waitFor(() => {
        expect(screen.getByTestId("shopping-page")).toBeInTheDocument();
      });

      // Navigate back to fridge
      const fridgeButton = screen.getByText("Navigate to Fridge");
      fireEvent.click(fridgeButton);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });
    });
  });

  describe("Fridge Item Operations", () => {
    it("should add a fridge item", async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const addButton = screen.getByText("Add Item");
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(FridgeService.addItemApi).toHaveBeenCalledWith({
          name: "Test",
          qty: 1
        });
        expect(FridgeService.fetchItems).toHaveBeenCalledTimes(2); // Initial + after add
      });
    });

    it("should handle add item error", async () => {
      const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation();
      FridgeService.addItemApi.mockRejectedValue(new Error("Add failed"));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const addButton = screen.getByText("Add Item");
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          "Failed to add fridge item",
          expect.any(Error)
        );
      });

      consoleErrorSpy.mockRestore();
    });

    it("should remove a fridge item", async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const removeButton = screen.getByText("Remove Item");
      fireEvent.click(removeButton);

      await waitFor(() => {
        expect(FridgeService.removeItemApi).toHaveBeenCalledWith(1);
      });
    });

    it("should handle remove item error and reload", async () => {
      const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation();
      FridgeService.removeItemApi.mockRejectedValue(new Error("Remove failed"));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const removeButton = screen.getByText("Remove Item");
      fireEvent.click(removeButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalled();
        expect(FridgeService.fetchItems).toHaveBeenCalledTimes(2); // Initial + after error
      });

      consoleErrorSpy.mockRestore();
    });

    it("should update a fridge item", async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const updateButton = screen.getByText("Update Item");
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(FridgeService.updateItemApi).toHaveBeenCalledWith(1, {
          id: 1,
          name: "Updated"
        });
        expect(FridgeService.fetchItems).toHaveBeenCalledTimes(2); // Initial + after update
      });
    });

    it("should handle update item error", async () => {
      const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation();
      FridgeService.updateItemApi.mockRejectedValue(new Error("Update failed"));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const updateButton = screen.getByText("Update Item");
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          "Failed to update fridge item",
          expect.any(Error)
        );
      });

      consoleErrorSpy.mockRestore();
    });
  });

  describe("Shopping List Operations", () => {
    beforeEach(async () => {
      render(<App />);
      
      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });

      const shoppingButton = screen.getByText("Navigate to Shopping");
      fireEvent.click(shoppingButton);

      await waitFor(() => {
        expect(screen.getByTestId("shopping-page")).toBeInTheDocument();
      });
    });

    it("should add a shopping item", async () => {
      const addButton = screen.getByText("Add Shopping Item");
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(ShoppingListService.addShoppingItem).toHaveBeenCalledWith({
          name: "Shop Item",
          qty: 1
        });
        expect(ShoppingListService.fetchShoppingItems).toHaveBeenCalledTimes(2);
      });
    });

    it("should handle add shopping item error", async () => {
      const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation();
      ShoppingListService.addShoppingItem.mockRejectedValue(new Error("Add failed"));

      const addButton = screen.getByText("Add Shopping Item");
      fireEvent.click(addButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          "Failed to add shopping item",
          expect.any(Error)
        );
      });

      consoleErrorSpy.mockRestore();
    });

    it("should remove a shopping item", async () => {
      const removeButton = screen.getByText("Remove Shopping Item");
      fireEvent.click(removeButton);

      await waitFor(() => {
        expect(ShoppingListService.removeShoppingItem).toHaveBeenCalledWith(1);
      });
    });

    it("should handle remove shopping item error", async () => {
      const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation();
      ShoppingListService.removeShoppingItem.mockRejectedValue(new Error("Remove failed"));

      const removeButton = screen.getByText("Remove Shopping Item");
      fireEvent.click(removeButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalled();
        expect(ShoppingListService.fetchShoppingItems).toHaveBeenCalledTimes(2);
      });

      consoleErrorSpy.mockRestore();
    });

    it("should mark item as bought", async () => {
      const markBoughtButton = screen.getByText("Mark Bought");
      fireEvent.click(markBoughtButton);

      await waitFor(() => {
        expect(ShoppingListService.markBought).toHaveBeenCalledWith(1);
        expect(FridgeService.fetchItems).toHaveBeenCalledTimes(2); // Item moved to fridge
        expect(ShoppingListService.fetchShoppingItems).toHaveBeenCalledTimes(2);
      });
    });

    it("should handle mark bought error", async () => {
      const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation();
      ShoppingListService.markBought.mockRejectedValue(new Error("Mark bought failed"));

      const markBoughtButton = screen.getByText("Mark Bought");
      fireEvent.click(markBoughtButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          "Failed to mark bought",
          expect.any(Error)
        );
      });

      consoleErrorSpy.mockRestore();
    });
  });

  describe("Data Import", () => {
    it("should reload data when import triggered", async () => {
      render(<App />);

      await waitFor(() => {
        expect(FridgeService.fetchItems).toHaveBeenCalledTimes(1);
        expect(ShoppingListService.fetchShoppingItems).toHaveBeenCalledTimes(1);
      });

      const loadDataButton = screen.getByText("Load Data");
      fireEvent.click(loadDataButton);

      await waitFor(() => {
        expect(FridgeService.fetchItems).toHaveBeenCalledTimes(2);
        expect(ShoppingListService.fetchShoppingItems).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe("Loading States", () => {
    it("should show loading state for fridge items", async () => {
      let resolveFetch;
      FridgeService.fetchItems.mockReturnValue(
        new Promise((resolve) => {
          resolveFetch = resolve;
        })
      );

      render(<App />);

      // At this point, loading should be true
      await waitFor(() => {
        expect(FridgeService.fetchItems).toHaveBeenCalled();
      });

      // Resolve the promise
      resolveFetch([]);

      await waitFor(() => {
        expect(screen.getByTestId("fridge-page")).toBeInTheDocument();
      });
    });

    it("should show loading state for shopping items", async () => {
      let resolveFetch;
      ShoppingListService.fetchShoppingItems.mockReturnValue(
        new Promise((resolve) => {
          resolveFetch = resolve;
        })
      );

      render(<App />);

      await waitFor(() => {
        expect(ShoppingListService.fetchShoppingItems).toHaveBeenCalled();
      });

      resolveFetch([]);

      await waitFor(() => {
        expect(screen.getByTestId("app-shell")).toBeInTheDocument();
      });
    });
  });
});