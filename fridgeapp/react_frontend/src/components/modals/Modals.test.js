import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import AddItemModal from "./AddItemModal";
import AddShoppingItemModal from "./AddShoppingItemModal";
import EditItemModal from "./EditItemModal";

describe("Modal Components", () => {
  describe("AddItemModal", () => {
    const mockOnClose = jest.fn();
    const mockOnAdd = jest.fn();

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it("should render with title", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      expect(screen.getByText("Add Item to Fridge")).toBeInTheDocument();
    });

    it("should have Cancel button", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      expect(screen.getByText("Cancel")).toBeInTheDocument();
    });

    it("should have Add Item button", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      expect(screen.getByText("Add Item")).toBeInTheDocument();
    });

    it("should call onClose when cancel button is clicked", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      expect(mockOnClose).toHaveBeenCalled();
    });

    it("should have input fields", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      const inputs = screen.getAllByRole("textbox");
      expect(inputs.length).toBeGreaterThan(0);
    });
  });

  describe("AddShoppingItemModal", () => {
    const mockOnClose = jest.fn();
    const mockOnAdd = jest.fn();

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it("should render with title", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      expect(screen.getByText("Add Item to Shopping List")).toBeInTheDocument();
    });

    it("should have Cancel button", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      expect(screen.getByText("Cancel")).toBeInTheDocument();
    });

    it("should have Add Item button", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      expect(screen.getByText("Add Item")).toBeInTheDocument();
    });

    it("should call onClose when cancel button is clicked", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  describe("EditItemModal", () => {
    const mockOnClose = jest.fn();
    const mockOnUpdate = jest.fn();
    const mockItem = {
      id: 1,
      name: "Test Item",
      quantity: 2,
      expirationDate: "2025-12-31"
    };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it("should render with title", () => {
      render(
        <EditItemModal 
          item={mockItem} 
          onClose={mockOnClose} 
          onUpdate={mockOnUpdate} 
        />
      );
      expect(screen.getByText("Edit Item")).toBeInTheDocument();
    });

    it("should have Cancel button", () => {
      render(
        <EditItemModal 
          item={mockItem} 
          onClose={mockOnClose} 
          onUpdate={mockOnUpdate} 
        />
      );
      expect(screen.getByText("Cancel")).toBeInTheDocument();
    });

    it("should call onClose when cancel button is clicked", () => {
      render(
        <EditItemModal 
          item={mockItem} 
          onClose={mockOnClose} 
          onUpdate={mockOnUpdate} 
        />
      );
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      expect(mockOnClose).toHaveBeenCalled();
    });

    it("should display item data", () => {
      render(
        <EditItemModal 
          item={mockItem} 
          onClose={mockOnClose} 
          onUpdate={mockOnUpdate} 
        />
      );
      expect(screen.getByDisplayValue("Test Item")).toBeInTheDocument();
    });
  });
});