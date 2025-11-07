import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import ShoppingList from "./ShoppingList";

describe("ShoppingList Component", () => {
  const defaultProps = {
    items: [
      { id: 1, name: "Eggs", quantity: 12, purchased: false },
      { id: 2, name: "Bread", quantity: 1, purchased: true }
    ],
    onAdd: jest.fn(),
    onRemove: jest.fn(),
    onTogglePurchased: jest.fn(),
    reload: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render the shopping list component", () => {
      const { container } = render(<ShoppingList {...defaultProps} />);
      expect(container.querySelector(".panel")).toBeInTheDocument();
    });

    it("should display all items", () => {
      render(<ShoppingList {...defaultProps} />);
      expect(screen.getByText("Eggs")).toBeInTheDocument();
      expect(screen.getByText("Bread")).toBeInTheDocument();
    });

    it("should render Add Item button", () => {
      render(<ShoppingList {...defaultProps} />);
      expect(screen.getByText("Add Item")).toBeInTheDocument();
    });

    it("should render Remove buttons", () => {
      render(<ShoppingList {...defaultProps} />);
      const removeButtons = screen.getAllByText("Remove");
      expect(removeButtons.length).toBeGreaterThan(0);
    });

    it("should display Search for item label", () => {
      render(<ShoppingList {...defaultProps} />);
      expect(screen.getByText("Search for item")).toBeInTheDocument();
    });
  });

  describe("Search", () => {
    it("should have search input", () => {
      render(<ShoppingList {...defaultProps} />);
      const searchInput = screen.getByPlaceholderText(/search/i);
      expect(searchInput).toBeInTheDocument();
    });

    it("should allow typing in search input", () => {
      render(<ShoppingList {...defaultProps} />);
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "Eggs" } });
      expect(searchInput.value).toBe("Eggs");
    });
  });

  describe("Remove Item", () => {
    it("should call onRemove when Remove button is clicked", () => {
      window.confirm = jest.fn(() => true);
      render(<ShoppingList {...defaultProps} />);
      
      const removeButtons = screen.getAllByText("Remove");
      fireEvent.click(removeButtons[0]);
      
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
    });
  });

  describe("Toggle Purchased", () => {
    it("should display checkboxes for items", () => {
      render(<ShoppingList {...defaultProps} />);
      const checkboxes = screen.getAllByRole("checkbox");
      expect(checkboxes.length).toBe(2);
    });

    it("should have clickable shopping items", () => {
      render(<ShoppingList {...defaultProps} />);
      const { container } = render(<ShoppingList {...defaultProps} />);
      const shoppingItems = container.querySelectorAll(".shopping-item");
      expect(shoppingItems.length).toBeGreaterThan(0);
    });
  });

  describe("Buttons", () => {
    it("should have Add All to Inventory button", () => {
      render(<ShoppingList {...defaultProps} />);
      expect(screen.getByText("Add All to Inventory")).toBeInTheDocument();
    });

    it("should have Remove All button", () => {
      render(<ShoppingList {...defaultProps} />);
      expect(screen.getByText("Remove All")).toBeInTheDocument();
    });

    it("should have Remove Item button", () => {
      render(<ShoppingList {...defaultProps} />);
      expect(screen.getByText("Remove Item")).toBeInTheDocument();
    });

    it("should have disabled Remove Item button when no items selected", () => {
      render(<ShoppingList {...defaultProps} />);
      const removeItemButton = screen.getByText("Remove Item");
      expect(removeItemButton).toBeDisabled();
    });
  });

  describe("Empty state", () => {
    it("should render when no items", () => {
      const emptyProps = {
        ...defaultProps,
        items: []
      };
      render(<ShoppingList {...emptyProps} />);
      expect(screen.getByText("Add Item")).toBeInTheDocument();
    });
  });
});