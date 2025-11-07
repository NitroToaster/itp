import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import ShoppingList from "./ShoppingList";

// Mock the modal component
jest.mock("../../components/modals/AddShoppingItemModal", () => {
  return function MockAddShoppingItemModal({ onClose, onAdd }) {
    return (
      <div data-testid="add-shopping-modal">
        <button onClick={() => onAdd({ name: "New Shopping Item", qty: 2 })}>
          Save
        </button>
        <button onClick={onClose}>Cancel</button>
      </div>
    );
  };
});

describe("ShoppingList Component", () => {
  const mockItems = [
    { id: 1, name: "Bread", qty: 2 },
    { id: 2, name: "Eggs", qty: 12 },
    { id: 3, name: "Butter", qty: 1 }
  ];

  const defaultProps = {
    items: mockItems,
    onAdd: jest.fn(),
    onRemove: jest.fn(),
    onMarkBought: jest.fn(),
    reload: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render shopping list items", () => {
      render(<ShoppingList {...defaultProps} />);
      
      expect(screen.getByText("Bread")).toBeInTheDocument();
      expect(screen.getByText("Eggs")).toBeInTheDocument();
      expect(screen.getByText("Butter")).toBeInTheDocument();
    });

    it("should render empty state when no items", () => {
      render(<ShoppingList {...defaultProps} items={[]} />);
      
      expect(screen.getByText(/No items/i)).toBeInTheDocument();
    });

    it("should render add item button", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const addButton = screen.getByText(/add.*item/i);
      expect(addButton).toBeInTheDocument();
    });
  });

  describe("Add Item", () => {
    it("should open add item modal", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const addButton = screen.getByText(/add.*item/i);
      fireEvent.click(addButton);
      
      expect(screen.getByTestId("add-shopping-modal")).toBeInTheDocument();
    });

    it("should call onAdd when item is added", async () => {
      render(<ShoppingList {...defaultProps} />);
      
      const addButton = screen.getByText(/add.*item/i);
      fireEvent.click(addButton);
      
      const saveButton = screen.getByText("Save");
      fireEvent.click(saveButton);
      
      await waitFor(() => {
        expect(defaultProps.onAdd).toHaveBeenCalled();
      });
    });

    it("should close modal when cancelled", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const addButton = screen.getByText(/add.*item/i);
      fireEvent.click(addButton);
      
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      
      expect(screen.queryByTestId("add-shopping-modal")).not.toBeInTheDocument();
    });
  });

  describe("Mark as Bought", () => {
    it("should call onMarkBought when mark bought button clicked", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const markBoughtButtons = screen.getAllByText(/bought/i);
      fireEvent.click(markBoughtButtons[0]);
      
      expect(defaultProps.onMarkBought).toHaveBeenCalledWith(1);
    });
  });

  describe("Remove Item", () => {
    it("should call onRemove when remove button clicked", () => {
      window.confirm = jest.fn(() => true);
      
      render(<ShoppingList {...defaultProps} />);
      
      const removeButtons = screen.getAllByText(/remove/i);
      fireEvent.click(removeButtons[0]);
      
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
    });

    it("should not call onRemove if user cancels", () => {
      window.confirm = jest.fn(() => false);
      
      render(<ShoppingList {...defaultProps} />);
      
      const removeButtons = screen.getAllByText(/remove/i);
      fireEvent.click(removeButtons[0]);
      
      expect(defaultProps.onRemove).not.toHaveBeenCalled();
    });
  });

  describe("Bulk Operations", () => {
    it("should select multiple items", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      fireEvent.click(checkboxes[1]);
      
      expect(checkboxes[0]).toBeChecked();
      expect(checkboxes[1]).toBeChecked();
    });

    it("should enable bulk buttons when items selected", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const markBoughtButton = screen.getByText(/mark.*bought/i);
      const removeButton = screen.getByText(/remove/i);
      
      expect(markBoughtButton).toBeDisabled();
      expect(removeButton).toBeDisabled();
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      expect(markBoughtButton).not.toBeDisabled();
      expect(removeButton).not.toBeDisabled();
    });

    it("should clear selection after bulk remove", async () => {
      const confirmSpy = jest.spyOn(window, "confirm").mockReturnValue(true);
      
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      fireEvent.click(checkboxes[1]);
      
      const removeButton = screen.getByText(/remove/i);
      fireEvent.click(removeButton);
      
      await waitFor(() => {
        expect(checkboxes[0]).not.toBeChecked();
        expect(checkboxes[1]).not.toBeChecked();
      });
      
      confirmSpy.mockRestore();
    });
  });

  describe("Reload Functionality", () => {
    it("should call reload when refresh button clicked", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const refreshButton = screen.getByText(/refresh/i);
      fireEvent.click(refreshButton);
      
      expect(defaultProps.reload).toHaveBeenCalled();
    });
  });

  describe("Edge Cases", () => {
    it("should handle items with special characters in names", () => {
      const specialItems = [
        { id: 1, name: "Peanut & Jelly", qty: 1 },
        { id: 2, name: "Salt/Pepper", qty: 1 }
      ];
      
      render(<ShoppingList {...defaultProps} items={specialItems} />);
      
      expect(screen.getByText("Peanut & Jelly")).toBeInTheDocument();
      expect(screen.getByText("Salt/Pepper")).toBeInTheDocument();
    });

    it("should handle items with zero quantity", () => {
      const zeroQtyItems = [
        { id: 1, name: "Test Item", qty: 0 }
      ];
      
      render(<ShoppingList {...defaultProps} items={zeroQtyItems} />);
      
      expect(screen.getByText("Test Item")).toBeInTheDocument();
      expect(screen.getByText(/0/)).toBeInTheDocument();
    });

    it("should handle items without quantity", () => {
      const noQtyItems = [
        { id: 1, name: "Test Item" }
      ];
      
      render(<ShoppingList {...defaultProps} items={noQtyItems} />);
      
      expect(screen.getByText("Test Item")).toBeInTheDocument();
    });
  });
});