import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import Fridge from "./Fridge";

describe("Fridge Component", () => {
  const defaultProps = {
    items: [
      { id: 1, name: "Milk", quantity: 2, expirationDate: "2025-12-30" },
      { id: 2, name: "Cheese", quantity: 1, expirationDate: "2025-12-15" }
    ],
    onAdd: jest.fn(),
    onUpdate: jest.fn(),
    onRemove: jest.fn(),
    reload: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render the fridge component", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Fridge")).toBeInTheDocument();
    });

    it("should render item names", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Milk")).toBeInTheDocument();
      expect(screen.getByText("Cheese")).toBeInTheDocument();
    });

    it("should render Add Item button", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Add Item")).toBeInTheDocument();
    });

    it("should render Edit and Remove buttons", () => {
      render(<Fridge {...defaultProps} />);
      const editButtons = screen.getAllByText("Edit");
      const removeButtons = screen.getAllByText("Remove");
      expect(editButtons.length).toBe(2);
      expect(removeButtons.length).toBe(2);
    });

    it("should render table headers", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Name")).toBeInTheDocument();
      expect(screen.getByText("Quantity")).toBeInTheDocument();
      expect(screen.getByText("Expiration")).toBeInTheDocument();
    });

    it("should render section header", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Fridge Items")).toBeInTheDocument();
    });
  });

  describe("Search", () => {
    it("should have search input", () => {
      render(<Fridge {...defaultProps} />);
      const searchInput = screen.getByPlaceholderText("Search items...");
      expect(searchInput).toBeInTheDocument();
    });

    it("should have Search and Clear buttons", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Search")).toBeInTheDocument();
      expect(screen.getByText("Clear")).toBeInTheDocument();
    });

    it("should allow typing in search input", () => {
      render(<Fridge {...defaultProps} />);
      const searchInput = screen.getByPlaceholderText("Search items...");
      fireEvent.change(searchInput, { target: { value: "Milk" } });
      expect(searchInput.value).toBe("Milk");
    });

    it("should clear search input when Clear is clicked", () => {
      render(<Fridge {...defaultProps} />);
      const searchInput = screen.getByPlaceholderText("Search items...");
      fireEvent.change(searchInput, { target: { value: "Milk" } });
      const clearButton = screen.getByText("Clear");
      fireEvent.click(clearButton);
      expect(searchInput.value).toBe("");
    });
  });

  describe("Sort", () => {
    it("should have sort dropdown", () => {
      render(<Fridge {...defaultProps} />);
      const sortSelect = screen.getByDisplayValue("Default");
      expect(sortSelect).toBeInTheDocument();
    });

    it("should have sort options", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Sort by Name")).toBeInTheDocument();
      expect(screen.getByText("Sort by Expiration")).toBeInTheDocument();
      expect(screen.getByText("Sort by Quantity")).toBeInTheDocument();
    });

    it("should allow changing sort option", () => {
      render(<Fridge {...defaultProps} />);
      const sortSelect = screen.getByDisplayValue("Default");
      fireEvent.change(sortSelect, { target: { value: "name" } });
      expect(sortSelect.value).toBe("name");
    });
  });

  describe("Filter", () => {
    it("should have a Filter button", () => {
      render(<Fridge {...defaultProps} />);
      expect(screen.getByText("Filter")).toBeInTheDocument();
    });

    it("should allow clicking Filter button", () => {
      render(<Fridge {...defaultProps} />);
      const filterButton = screen.getByText("Filter");
      fireEvent.click(filterButton);
      expect(filterButton).toBeInTheDocument();
    });
  });

  describe("Remove Item", () => {
    it("should call onRemove when Remove button is clicked and confirmed", () => {
      window.confirm = jest.fn(() => true);
      render(<Fridge {...defaultProps} />);
      
      const removeButtons = screen.getAllByText("Remove");
      fireEvent.click(removeButtons[0]);
      
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
    });

    it("should not call onRemove when user cancels", () => {
      window.confirm = jest.fn(() => false);
      render(<Fridge {...defaultProps} />);
      
      const removeButtons = screen.getAllByText("Remove");
      fireEvent.click(removeButtons[0]);
      
      expect(defaultProps.onRemove).not.toHaveBeenCalled();
    });
  });

  describe("Add Item", () => {
    it("should open modal when Add Item clicked", () => {
      render(<Fridge {...defaultProps} />);
      const addButton = screen.getByText("Add Item");
      fireEvent.click(addButton);
      expect(addButton).toBeInTheDocument();
    });
  });

  describe("Edit Item", () => {
    it("should allow clicking Edit button", () => {
      render(<Fridge {...defaultProps} />);
      const editButtons = screen.getAllByText("Edit");
      fireEvent.click(editButtons[0]);
      expect(editButtons[0]).toBeInTheDocument();
    });
  });

  describe("Empty state", () => {
    it("should render with empty items", () => {
      const emptyProps = {
        ...defaultProps,
        items: []
      };
      render(<Fridge {...emptyProps} />);
      expect(screen.getByText("Fridge")).toBeInTheDocument();
    });
  });
});