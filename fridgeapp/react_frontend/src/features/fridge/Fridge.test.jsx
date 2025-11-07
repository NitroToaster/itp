import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import Fridge from "./Fridge";

// Mock the modal components
jest.mock("../../components/modals/AddItemModal", () => {
  return function MockAddItemModal({ onClose, onAdd }) {
    return (
      <div data-testid="add-item-modal">
        <button onClick={() => onAdd({ name: "New Item", qty: 1, expiration: "2025-11-01" })}>
          Save
        </button>
        <button onClick={onClose}>Cancel</button>
      </div>
    );
  };
});

jest.mock("../../components/modals/EditItemModal", () => {
  return function MockEditItemModal({ item, onClose, onUpdate }) {
    return (
      <div data-testid="edit-item-modal">
        <button onClick={() => onUpdate({ ...item, name: "Updated Item" })}>
          Update
        </button>
        <button onClick={onClose}>Cancel</button>
      </div>
    );
  };
});

describe("Fridge Component", () => {
  const mockItems = [
    { id: 1, name: "Milk", qty: 2, expiration: "2025-10-30" },
    { id: 2, name: "Cheese", qty: 1, expiration: "2025-11-05" }
  ];

  const defaultProps = {
    items: mockItems,
    onAdd: jest.fn(),
    onRemove: jest.fn(),
    onUpdate: jest.fn(),
    reload: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render fridge items", () => {
      render(<Fridge {...defaultProps} />);
      
      expect(screen.getByText("Milk")).toBeInTheDocument();
      expect(screen.getByText("Cheese")).toBeInTheDocument();
    });

    it("should render empty state when no items", () => {
      render(<Fridge {...defaultProps} items={[]} />);
      
      expect(screen.getByText(/No items/i)).toBeInTheDocument();
    });

    it("should render search bar", () => {
      render(<Fridge {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      expect(searchInput).toBeInTheDocument();
    });

    it("should render add item button", () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      expect(addButton).toBeInTheDocument();
    });
  });

  describe("Search Functionality", () => {
    it("should filter items by search term", () => {
      render(<Fridge {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "Milk" } });
      
      expect(screen.getByText("Milk")).toBeInTheDocument();
      expect(screen.queryByText("Cheese")).not.toBeInTheDocument();
    });

    it("should clear search", () => {
      render(<Fridge {...defaultProps} />);
      
      const searchInput = screen.getByPlaceholderText(/search/i);
      fireEvent.change(searchInput, { target: { value: "Milk" } });
      
      const clearButton = screen.getByText(/clear/i);
      fireEvent.click(clearButton);
      
      expect(searchInput.value).toBe("");
      expect(screen.getByText("Milk")).toBeInTheDocument();
      expect(screen.getByText("Cheese")).toBeInTheDocument();
    });
  });

  describe("Add Item", () => {
    it("should open add item modal", () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      expect(screen.getByTestId("add-item-modal")).toBeInTheDocument();
    });

    it("should call onAdd when item is added", async () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      const saveButton = screen.getByText("Save");
      fireEvent.click(saveButton);
      
      await waitFor(() => {
        expect(defaultProps.onAdd).toHaveBeenCalled();
      });
    });

    it("should close modal when cancelled", () => {
      render(<Fridge {...defaultProps} />);
      
      const addButton = screen.getByText(/add item/i);
      fireEvent.click(addButton);
      
      const cancelButton = screen.getByText("Cancel");
      fireEvent.click(cancelButton);
      
      expect(screen.queryByTestId("add-item-modal")).not.toBeInTheDocument();
    });
  });

  describe("Edit Item", () => {
    it("should open edit modal when edit button clicked", () => {
      render(<Fridge {...defaultProps} />);
      
      const editButtons = screen.getAllByText(/edit/i);
      fireEvent.click(editButtons[0]);
      
      expect(screen.getByTestId("edit-item-modal")).toBeInTheDocument();
    });

    it("should call onUpdate when item is updated", async () => {
      render(<Fridge {...defaultProps} />);
      
      const editButtons = screen.getAllByText(/edit/i);
      fireEvent.click(editButtons[0]);
      
      const updateButton = screen.getByText("Update");
      fireEvent.click(updateButton);
      
      await waitFor(() => {
        expect(defaultProps.onUpdate).toHaveBeenCalled();
      });
    });
  });

  describe("Remove Item", () => {
    it("should call onRemove when remove button clicked", () => {
      window.confirm = jest.fn(() => true);
      
      render(<Fridge {...defaultProps} />);
      
      const removeButtons = screen.getAllByText(/remove/i);
      fireEvent.click(removeButtons[0]);
      
      expect(defaultProps.onRemove).toHaveBeenCalledWith(1);
    });

    it("should not call onRemove if user cancels", () => {
      window.confirm = jest.fn(() => false);
      
      render(<Fridge {...defaultProps} />);
      
      const removeButtons = screen.getAllByText(/remove/i);
      fireEvent.click(removeButtons[0]);
      
      expect(defaultProps.onRemove).not.toHaveBeenCalled();
    });
  });

  describe("Sorting", () => {
    it("should sort items by name", () => {
      render(<Fridge {...defaultProps} />);
      
      const sortSelect = screen.getByRole("combobox");
      fireEvent.change(sortSelect, { target: { value: "name" } });
      
      const items = screen.getAllByRole("article");
      // First item should be Cheese (alphabetically first)
      expect(items[0]).toHaveTextContent("Cheese");
    });

    it("should sort items by expiration", () => {
      render(<Fridge {...defaultProps} />);
      
      const sortSelect = screen.getByRole("combobox");
      fireEvent.change(sortSelect, { target: { value: "expiration" } });
      
      const items = screen.getAllByRole("article");
      // First item should be Milk (expires first)
      expect(items[0]).toHaveTextContent("Milk");
    });
  });

  describe("Reload", () => {
    it("should call reload when refresh button clicked", () => {
      render(<Fridge {...defaultProps} />);
      
      const refreshButton = screen.getByText(/refresh/i);
      fireEvent.click(refreshButton);
      
      expect(defaultProps.reload).toHaveBeenCalled();
    });
  });
});