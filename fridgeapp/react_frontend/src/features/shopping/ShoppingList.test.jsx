import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import ShoppingList from "../features/shopping/ShoppingList";

// Mock the modal component
jest.mock("../components/modals/AddShoppingItemModal", () => {
  return function MockAddShoppingItemModal({ onClose, onAdd }) {
    return (
      <div data-testid="add-shopping-modal">
        <button
          onClick={() => {
            onAdd({ name: "New Shopping Item", qty: 1 });
            onClose();
          }}
        >
          Submit Add
        </button>
        <button onClick={onClose}>Cancel</button>
      </div>
    );
  };
});

describe("ShoppingList Component", () => {
  const mockItems = [
    { id: 1, name: "Bread", qty: 2 },
    { id: 2, name: "Butter", qty: 1 },
    { id: 3, name: "Bananas", qty: 5 }
  ];

  const defaultProps = {
    items: mockItems,
    loading: false,
    onRemove: jest.fn(),
    onAdd: jest.fn(),
    onMarkBought: jest.fn(),
    reload: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render the shopping list page", () => {
      render(<ShoppingList {...defaultProps} />);
      
      expect(screen.getByText("Bread")).toBeInTheDocument();
      expect(screen.getByText("Butter")).toBeInTheDocument();
      expect(screen.getByText("Bananas")).toBeInTheDocument();
    });

    it("should show loading state", () => {
      render(<ShoppingList {...defaultProps} loading={true} />);
      
      expect(screen.getByText(/loading/i)).toBeInTheDocument();
    });

    it("should show empty state when no items", () => {
      render(<ShoppingList {...defaultProps} items={[]} />);
      
      expect(screen.getByText(/no items/i)).toBeInTheDocument();
    });

    it("should display item quantities", () => {
      render(<ShoppingList {...defaultProps} />);
      
      expect(screen.getByText(/2/)).toBeInTheDocument(); // Bread qty
      expect(screen.getByText(/1/)).toBeInTheDocument(); // Butter qty
      expect(screen.getByText(/5/)).toBeInTheDocument(); // Bananas qty
    });
  });

  describe("Prefix Search Functionality", () => {
    it("should filter items by prefix search", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "Br" } });
      
      expect(screen.getByText("Bread")).toBeInTheDocument();
      expect(screen.queryByText("Butter")).not.toBeInTheDocument();
      expect(screen.queryByText("Bananas")).not.toBeInTheDocument();
    });

    it("should be case insensitive", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "br" } });
      
      expect(screen.getByText("Bread")).toBeInTheDocument();
    });

    it("should show all items when search is cleared", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "Br" } });
      fireEvent.change(searchInput, { target: { value: "" } });
      
      expect(screen.getByText("Bread")).toBeInTheDocument();
      expect(screen.getByText("Butter")).toBeInTheDocument();
      expect(screen.getByText("Bananas")).toBeInTheDocument();
    });

    it("should show no results message when search returns nothing", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "Xyz" } });
      
      expect(screen.getByText(/no items found/i)).toBeInTheDocument();
    });

    it("should only match prefix, not substring", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "read" } });
      
      // "read" is in "Bread" but not at the start
      expect(screen.queryByText("Bread")).not.toBeInTheDocument();
    });
  });

  describe("Multi-Select Functionality", () => {
    it("should select an item when checkbox clicked", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      expect(checkboxes[0]).toBeChecked();
    });

    it("should deselect an item when checkbox clicked again", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      fireEvent.click(checkboxes[0]);
      
      expect(checkboxes[0]).not.toBeChecked();
    });

    it("should select multiple items", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      fireEvent.click(checkboxes[1]);
      
      expect(checkboxes[0]).toBeChecked();
      expect(checkboxes[1]).toBeChecked();
    });

    it("should show selection count", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      fireEvent.click(checkboxes[1]);
      
      expect(screen.getByText(/2 selected/i)).toBeInTheDocument();
    });

    it("should select all items with select all button", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const selectAllButton = screen.getByText(/select all/i);
      fireEvent.click(selectAllButton);
      
      const checkboxes = screen.getAllByRole("checkbox");
      checkboxes.forEach(checkbox => {
        expect(checkbox).toBeChecked();
      });
    });

    it("should deselect all items with deselect all button", () => {
      render(<ShoppingList {...defaultProps} />);
      
      // First select all
      const selectAllButton = screen.getByText(/select all/i);
      fireEvent.click(selectAllButton);
      
      // Then deselect all
      const deselectAllButton = screen.getByText(/deselect all/i);
      fireEvent.click(deselectAllButton);
      
      const checkboxes = screen.getAllByRole("checkbox");
      checkboxes.forEach(checkbox => {
        expect(checkbox).not.toBeChecked();
      });
    });
  });

  describe("Add Item Functionality", () => {
    it("should open add item modal", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      expect(screen.getByTestId("add-shopping-modal")).toBeInTheDocument();
    });

    it("should close add item modal on cancel", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      
      expect(screen.queryByTestId("add-shopping-modal")).not.toBeInTheDocument();
    });

    it("should call onAdd when submitting new item", async () => {
      render(<ShoppingList {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      const submitButton = screen.getByText("Submit Add");
      fireEvent.click(submitButton);
      
      await waitFor(() => {
        expect(defaultProps.onAdd).toHaveBeenCalledWith({
          name: "New Shopping Item",
          qty: 1
        });
      });
    });
  });

  describe("Mark Bought Functionality", () => {
    it("should call onMarkBought for single item", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      const markBoughtButton = screen.getByText(/mark bought/i);
      fireEvent.click(markBoughtButton);
      
      expect(defaultProps.onMarkBought).toHaveBeenCalledWith(1);
    });

    it("should call onMarkBought for multiple items", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      fireEvent.click(checkboxes[1]);
      
      const markBoughtButton = screen.getByText(/mark bought/i);
      fireEvent.click(markBoughtButton);
      
      expect(defaultProps.onMarkBought).toHaveBeenCalledTimes(2);
      expect(defaultProps.onMarkBought).toHaveBeenCalledWith(1);
      expect(defaultProps.onMarkBought).toHaveBeenCalledWith(2);
    });

    it("should clear selection after marking bought", async () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      const markBoughtButton = screen.getByText(/mark bought/i);
      fireEvent.click(markBoughtButton);
      
      await waitFor(() => {
        expect(checkboxes[0]).not.toBeChecked();
      });
    });

    it("should disable mark bought button when nothing selected", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const markBoughtButton = screen.getByText(/mark bought/i);
      
      expect(markBoughtButton).toBeDisabled();
    });

    it("should enable mark bought button when item selected", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      const markBoughtButton = screen.getByText(/mark bought/i);
      
      expect(markBoughtButton).not.toBeDisabled();
    });
  });

  describe("Remove Item Functionality", () => {
    it("should call onRemove for single item", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      const removeButton = screen.getByText(/remove/i);
      fireEvent.click(removeButton);
      
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
    });

    it("should call onRemove for multiple items", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      fireEvent.click(checkboxes[2]);
      
      const removeButton = screen.getByText(/remove/i);
      fireEvent.click(removeButton);
      
      expect(defaultProps.onRemove).toHaveBeenCalledTimes(2);
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
      expect(defaultProps.onRemove).toHaveBeenCalledWith(3);
    });

    it("should confirm before removing", () => {
      const confirmSpy = jest.spyOn(window, "confirm").mockReturnValue(true);
      
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      const removeButton = screen.getByText(/remove/i);
      fireEvent.click(removeButton);
      
      expect(confirmSpy).toHaveBeenCalled();
      expect(defaultProps.onRemove).toHaveBeenCalled();
      
      confirmSpy.mockRestore();
    });

    it("should not remove if confirmation cancelled", () => {
      const confirmSpy = jest.spyOn(window, "confirm").mockReturnValue(false);
      
      render(<ShoppingList {...defaultProps} />);
      
      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);
      
      const removeButton = screen.getByText(/remove/i);
      fireEvent.click(removeButton);
      
      expect(confirmSpy).toHaveBeenCalled();
      expect(defaultProps.onRemove).not.toHaveBeenCalled();
      
      confirmSpy.mockRestore();
    });

    it("should disable remove button when nothing selected", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const removeButton = screen.getByText(/remove/i);
      
      expect(removeButton).toBeDisabled();
    });
  });

  describe("Bulk Operations", () => {
    it("should enable bulk action buttons only when items selected", () => {
      render(<ShoppingList {...defaultProps} />);
      
      const markBoughtButton = screen.getByText(/mark bought/i);
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