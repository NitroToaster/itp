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
      render(<ShoppingList {...defaultProps} />);
      expect(screen.getByText("Shopping List")).toBeInTheDocument();
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
  });

  describe("Search", () => {
    it("should have search input", () => {
      render(<ShoppingList {...defaultProps} />);
      const searchInput = screen.getByPlaceholderText(/search/i);
      expect(searchInput).toBeInTheDocument();
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

    it("should call onTogglePurchased when checkbox is clicked", () => {
      render(<ShoppingList {...defaultProps} />);
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      expect(defaultProps.onTogglePurchased).toHaveBeenCalledWith(1);
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
  });
});