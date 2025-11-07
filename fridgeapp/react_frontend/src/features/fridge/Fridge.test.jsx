import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import Fridge from "../features/fridge/Fridge";

// Mock the modal components
jest.mock("../components/modals/AddItemModal", () => {
  return function MockAddItemModal({ onClose, onAdd }) {
    return (
      <div data-testid="add-item-modal">
        <button
          onClick={() => {
            onAdd({ name: "New Item", qty: 1, expiration: "2025-12-01" });
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

jest.mock("../components/modals/EditItemModal", () => {
  return function MockEditItemModal({ item, onClose, onSave }) {
    return (
      <div data-testid="edit-item-modal">
        <span>Editing: {item.name}</span>
        <button
          onClick={() => {
            onSave({ ...item, name: "Updated Item" });
            onClose();
          }}
        >
          Submit Edit
        </button>
        <button onClick={onClose}>Cancel</button>
      </div>
    );
  };
});

describe("Fridge Component", () => {
  const mockItems = [
    { id: 1, name: "Milk", qty: 2, expiration: "2025-10-30" },
    { id: 2, name: "Eggs", qty: 12, expiration: "2025-11-15" },
    { id: 3, name: "Cheese", qty: 1, expiration: "2025-10-25" }
  ];

  const defaultProps = {
    items: mockItems,
    loading: false,
    onAdd: jest.fn(),
    onRemove: jest.fn(),
    onUpdate: jest.fn(),
    reload: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render the fridge page", () => {
      render(<Fridge {...defaultProps} />);
      
      expect(screen.getByText("Milk")).toBeInTheDocument();
      expect(screen.getByText("Eggs")).toBeInTheDocument();
      expect(screen.getByText("Cheese")).toBeInTheDocument();
    });

    it("should show loading state", () => {
      render(<Fridge {...defaultProps} loading={true} />);
      
      expect(screen.getByText(/loading/i)).toBeInTheDocument();
    });

    it("should show empty state when no items", () => {
      render(<Fridge {...defaultProps} items={[]} />);
      
      expect(screen.getByText(/no items/i)).toBeInTheDocument();
    });

    it("should display item quantities", () => {
      render(<Fridge {...defaultProps} />);
      
      expect(screen.getByText(/2/)).toBeInTheDocument(); // Milk qty
      expect(screen.getByText(/12/)).toBeInTheDocument(); // Eggs qty
    });

    it("should display expiration dates", () => {
      render(<Fridge {...defaultProps} />);
      
      expect(screen.getByText(/2025-10-30/)).toBeInTheDocument();
      expect(screen.getByText(/2025-11-15/)).toBeInTheDocument();
    });
  });

  describe("Search Functionality", () => {
    it("should filter items by search query", () => {
      render(<Fridge {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "milk" } });
      
      expect(screen.getByText("Milk")).toBeInTheDocument();
      expect(screen.queryByText("Eggs")).not.toBeInTheDocument();
      expect(screen.queryByText("Cheese")).not.toBeInTheDocument();
    });

    it("should be case insensitive", () => {
      render(<Fridge {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "MILK" } });
      
      expect(screen.getByText("Milk")).toBeInTheDocument();
    });

    it("should show all items when search is cleared", () => {
      render(<Fridge {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "milk" } });
      fireEvent.change(searchInput, { target: { value: "" } });
      
      expect(screen.getByText("Milk")).toBeInTheDocument();
      expect(screen.getByText("Eggs")).toBeInTheDocument();
      expect(screen.getByText("Cheese")).toBeInTheDocument();
    });

    it("should show no results message when search returns nothing", () => {
      render(<Fridge {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "nonexistent" } });
      
      expect(screen.getByText(/no items found/i)).toBeInTheDocument();
    });
  });

  describe("Sort Functionality", () => {
    it("should sort items by name ascending", () => {
      render(<Fridge {...defaultProps} />);
      
      const sortSelect = screen.getByRole("combobox");
      fireEvent.change(sortSelect, { target: { value: "name-asc" } });
      
      const items = screen.getAllByRole("listitem");
      expect(items[0]).toHaveTextContent("Cheese");
      expect(items[1]).toHaveTextContent("Eggs");
      expect(items[2]).toHaveTextContent("Milk");
    });

    it("should sort items by expiration date", () => {
      render(<Fridge {...defaultProps} />);
      
      const sortSelect = screen.getByRole("combobox");
      fireEvent.change(sortSelect, { target: { value: "expiration" } });
      
      const items = screen.getAllByRole("listitem");
      // Cheese expires first (10-25), then Milk (10-30), then Eggs (11-15)
      expect(items[0]).toHaveTextContent("Cheese");
      expect(items[1]).toHaveTextContent("Milk");
      expect(items[2]).toHaveTextContent("Eggs");
    });

    it("should sort items by quantity", () => {
      render(<Fridge {...defaultProps} />);
      
      const sortSelect = screen.getByRole("combobox");
      fireEvent.change(sortSelect, { target: { value: "quantity" } });
      
      const items = screen.getAllByRole("listitem");
      // Cheese: 1, Milk: 2, Eggs: 12
      expect(items[0]).toHaveTextContent("Cheese");
      expect(items[1]).toHaveTextContent("Milk");
      expect(items[2]).toHaveTextContent("Eggs");
    });
  });

  describe("Expiration Color Indicators", () => {
    it("should mark expired items as red", () => {
      const expiredItems = [
        { id: 1, name: "Old Milk", qty: 1, expiration: "2020-01-01" }
      ];
      
      render(<Fridge {...defaultProps} items={expiredItems} />);
      
      const item = screen.getByText("Old Milk").closest("li");
      expect(item).toHaveClass("expired");
    });

    it("should mark items expiring soon as yellow", () => {
      const today = new Date();
      const soonDate = new Date(today);
      soonDate.setDate(today.getDate() + 2);
      
      const soonItems = [
        { 
          id: 1, 
          name: "Soon Milk", 
          qty: 1, 
          expiration: soonDate.toISOString().split("T")[0] 
        }
      ];
      
      render(<Fridge {...defaultProps} items={soonItems} />);
      
      const item = screen.getByText("Soon Milk").closest("li");
      expect(item).toHaveClass("expiring-soon");
    });

    it("should mark fresh items as green", () => {
      const today = new Date();
      const futureDate = new Date(today);
      futureDate.setDate(today.getDate() + 30);
      
      const freshItems = [
        { 
          id: 1, 
          name: "Fresh Milk", 
          qty: 1, 
          expiration: futureDate.toISOString().split("T")[0] 
        }
      ];
      
      render(<Fridge {...defaultProps} items={freshItems} />);
      
      const item = screen.getByText("Fresh Milk").closest("li");
      expect(item).toHaveClass("fresh");
    });
  });

  describe("Add Item Functionality", () => {
    it("should open add item modal", () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      expect(screen.getByTestId("add-item-modal")).toBeInTheDocument();
    });

    it("should close add item modal on cancel", () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      
      expect(screen.queryByTestId("add-item-modal")).not.toBeInTheDocument();
    });

    it("should call onAdd when submitting new item", async () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      const submitButton = screen.getByText("Submit Add");
      fireEvent.click(submitButton);
      
      await waitFor(() => {
        expect(defaultProps.onAdd).toHaveBeenCalledWith({
          name: "New Item",
          qty: 1,
          expiration: "2025-12-01"
        });
      });
    });

    it("should close modal after successful add", async () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      const submitButton = screen.getByText("Submit Add");
      fireEvent.click(submitButton);
      
      await waitFor(() => {
        expect(screen.queryByTestId("add-item-modal")).not.toBeInTheDocument();
      });
    });
  });

  describe("Edit Item Functionality", () => {
    it("should open edit modal for specific item", () => {
      render(<Fridge {...defaultProps} />);
      
      const editButtons = screen.getAllByText(/edit/i);
      fireEvent.click(editButtons[0]);
      
      expect(screen.getByTestId("edit-item-modal")).toBeInTheDocument();
      expect(screen.getByText("Editing: Milk")).toBeInTheDocument();
    });

    it("should close edit modal on cancel", () => {
      render(<Fridge {...defaultProps} />);
      
      const editButtons = screen.getAllByText(/edit/i);
      fireEvent.click(editButtons[0]);
      
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      
      expect(screen.queryByTestId("edit-item-modal")).not.toBeInTheDocument();
    });

    it("should call onUpdate when submitting edited item", async () => {
      render(<Fridge {...defaultProps} />);
      
      const editButtons = screen.getAllByText(/edit/i);
      fireEvent.click(editButtons[0]);
      
      const submitButton = screen.getByText("Submit Edit");
      fireEvent.click(submitButton);
      
      await waitFor(() => {
        expect(defaultProps.onUpdate).toHaveBeenCalledWith(
          expect.objectContaining({
            id: 1,
            name: "Updated Item"
          })
        );
      });
    });
  });

  describe("Remove Item Functionality", () => {
    it("should call onRemove when delete button clicked", () => {
      render(<Fridge {...defaultProps} />);
      
      const deleteButtons = screen.getAllByText(/delete/i);
      fireEvent.click(deleteButtons[0]);
      
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
    });

    it("should confirm before deleting", () => {
      const confirmSpy = jest.spyOn(window, "confirm").mockReturnValue(true);
      
      render(<Fridge {...defaultProps} />);
      
      const deleteButtons = screen.getAllByText(/delete/i);
      fireEvent.click(deleteButtons[0]);
      
      expect(confirmSpy).toHaveBeenCalled();
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
      
      confirmSpy.mockRestore();
    });

    it("should not delete if confirmation cancelled", () => {
      const confirmSpy = jest.spyOn(window, "confirm").mockReturnValue(false);
      
      render(<Fridge {...defaultProps} />);
      
      const deleteButtons = screen.getAllByText(/delete/i);
      fireEvent.click(deleteButtons[0]);
      
      expect(confirmSpy).toHaveBeenCalled();
      expect(defaultProps.onRemove).not.toHaveBeenCalled();
      
      confirmSpy.mockRestore();
    });
  });

  describe("Reload Functionality", () => {
    it("should call reload when refresh button clicked", () => {
      render(<Fridge {...defaultProps} />);
      
      const refreshButton = screen.getByText(/refresh/i);
      fireEvent.click(refreshButton);
      
      expect(defaultProps.reload).toHaveBeenCalled();
    });
  });
});