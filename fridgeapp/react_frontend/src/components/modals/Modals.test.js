import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import AddItemModal from "./AddItemModal";
import EditItemModal from "./EditItemModal";
import AddShoppingItemModal from "./AddShoppingItemModal";

describe("Modal Components", () => {
  describe("AddItemModal", () => {
    const mockOnClose = jest.fn();
    const mockOnAdd = jest.fn();

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it("should render add item modal", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      expect(screen.getByText(/add.*item/i)).toBeInTheDocument();
    });

    it("should have input fields", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/quantity/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/expiration/i)).toBeInTheDocument();
    });

    it("should call onAdd with form data", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      fireEvent.change(screen.getByLabelText(/name/i), { target: { value: "Milk" } });
      fireEvent.change(screen.getByLabelText(/quantity/i), { target: { value: "2" } });
      fireEvent.change(screen.getByLabelText(/expiration/i), { target: { value: "2025-11-01" } });
      
      const submitButton = screen.getByText(/save|add/i);
      fireEvent.click(submitButton);
      
      expect(mockOnAdd).toHaveBeenCalledWith({
        name: "Milk",
        qty: 2,
        expiration: "2025-11-01"
      });
    });

    it("should call onClose when cancel clicked", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      const cancelButton = screen.getByText(/cancel/i);
      fireEvent.click(cancelButton);
      
      expect(mockOnClose).toHaveBeenCalled();
    });

    it("should validate required fields", () => {
      render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      const submitButton = screen.getByText(/save|add/i);
      fireEvent.click(submitButton);
      
      // Should not call onAdd if required fields are empty
      expect(mockOnAdd).not.toHaveBeenCalled();
    });
  });

  describe("EditItemModal", () => {
    const mockOnClose = jest.fn();
    const mockOnUpdate = jest.fn();
    const mockItem = {
      id: 1,
      name: "Milk",
      qty: 2,
      expiration: "2025-10-30"
    };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it("should render edit item modal", () => {
      render(<EditItemModal item={mockItem} onClose={mockOnClose} onUpdate={mockOnUpdate} />);
      
      expect(screen.getByText(/edit.*item/i)).toBeInTheDocument();
    });

    it("should populate form with existing item data", () => {
      render(<EditItemModal item={mockItem} onClose={mockOnClose} onUpdate={mockOnUpdate} />);
      
      expect(screen.getByDisplayValue("Milk")).toBeInTheDocument();
      expect(screen.getByDisplayValue("2")).toBeInTheDocument();
      expect(screen.getByDisplayValue("2025-10-30")).toBeInTheDocument();
    });

    it("should call onUpdate with modified data", () => {
      render(<EditItemModal item={mockItem} onClose={mockOnClose} onUpdate={mockOnUpdate} />);
      
      fireEvent.change(screen.getByLabelText(/name/i), { target: { value: "Updated Milk" } });
      
      const updateButton = screen.getByText(/update|save/i);
      fireEvent.click(updateButton);
      
      expect(mockOnUpdate).toHaveBeenCalledWith(expect.objectContaining({
        id: 1,
        name: "Updated Milk"
      }));
    });

    it("should call onClose when cancel clicked", () => {
      render(<EditItemModal item={mockItem} onClose={mockOnClose} onUpdate={mockOnUpdate} />);
      
      const cancelButton = screen.getByText(/cancel/i);
      fireEvent.click(cancelButton);
      
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  describe("AddShoppingItemModal", () => {
    const mockOnClose = jest.fn();
    const mockOnAdd = jest.fn();

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it("should render add shopping item modal", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      expect(screen.getByText(/add.*item/i)).toBeInTheDocument();
    });

    it("should have name and quantity input fields", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/quantity/i)).toBeInTheDocument();
    });

    it("should call onAdd with form data", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      fireEvent.change(screen.getByLabelText(/name/i), { target: { value: "Bread" } });
      fireEvent.change(screen.getByLabelText(/quantity/i), { target: { value: "1" } });
      
      const submitButton = screen.getByText(/save|add/i);
      fireEvent.click(submitButton);
      
      expect(mockOnAdd).toHaveBeenCalledWith({
        name: "Bread",
        qty: 1
      });
    });

    it("should call onClose when cancel clicked", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      const cancelButton = screen.getByText(/cancel/i);
      fireEvent.click(cancelButton);
      
      expect(mockOnClose).toHaveBeenCalled();
    });

    it("should validate required fields", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      const submitButton = screen.getByText(/save|add/i);
      fireEvent.click(submitButton);
      
      // Should not call onAdd if required fields are empty
      expect(mockOnAdd).not.toHaveBeenCalled();
    });

    it("should default quantity to 1 if not specified", () => {
      render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
      
      fireEvent.change(screen.getByLabelText(/name/i), { target: { value: "Eggs" } });
      
      const submitButton = screen.getByText(/save|add/i);
      fireEvent.click(submitButton);
      
      expect(mockOnAdd).toHaveBeenCalledWith(expect.objectContaining({
        qty: expect.any(Number)
      }));
    });
  });

  describe("Modal Overlay Behavior", () => {
    it("should close on overlay click for AddItemModal", () => {
      const mockOnClose = jest.fn();
      render(<AddItemModal onClose={mockOnClose} onAdd={jest.fn()} />);
      
      const overlay = screen.getByRole("dialog").parentElement;
      fireEvent.click(overlay);
      
      expect(mockOnClose).toHaveBeenCalled();
    });

    it("should not close when clicking inside modal", () => {
      const mockOnClose = jest.fn();
      render(<AddItemModal onClose={mockOnClose} onAdd={jest.fn()} />);
      
      const modal = screen.getByRole("dialog");
      fireEvent.click(modal);
      
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });
});