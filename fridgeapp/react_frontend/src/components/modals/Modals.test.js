import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import AddItemModal from "../components/modals/AddItemModal";
import EditItemModal from "../components/modals/EditItemModal";
import AddShoppingItemModal from "../components/modals/AddShoppingItemModal";

describe("AddItemModal Component", () => {
  const mockOnClose = jest.fn();
  const mockOnAdd = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render the modal", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    expect(screen.getByText(/add item/i)).toBeInTheDocument();
  });

  it("should have name input field", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    expect(nameInput).toBeInTheDocument();
    expect(nameInput).toHaveAttribute("required");
  });

  it("should have quantity input field", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const qtyInput = screen.getByLabelText(/quantity/i);
    expect(qtyInput).toBeInTheDocument();
    expect(qtyInput).toHaveAttribute("type", "number");
  });

  it("should have expiration date input field", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const expirationInput = screen.getByLabelText(/expiration/i);
    expect(expirationInput).toBeInTheDocument();
    expect(expirationInput).toHaveAttribute("type", "date");
  });

  it("should call onClose when cancel button clicked", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const cancelButton = screen.getByText(/cancel/i);
    fireEvent.click(cancelButton);
    
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("should call onAdd with form data when submitted", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    const qtyInput = screen.getByLabelText(/quantity/i);
    const expirationInput = screen.getByLabelText(/expiration/i);
    const submitButton = screen.getByText(/add/i);
    
    fireEvent.change(nameInput, { target: { value: "Milk" } });
    fireEvent.change(qtyInput, { target: { value: "2" } });
    fireEvent.change(expirationInput, { target: { value: "2025-12-31" } });
    fireEvent.click(submitButton);
    
    expect(mockOnAdd).toHaveBeenCalledWith({
      name: "Milk",
      qty: 2,
      expiration: "2025-12-31"
    });
  });

  it("should not submit without required name field", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const submitButton = screen.getByText(/add/i);
    fireEvent.click(submitButton);
    
    // Form should not submit due to HTML5 validation
    expect(mockOnAdd).not.toHaveBeenCalled();
  });

  it("should submit with only name field (optional fields empty)", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    const submitButton = screen.getByText(/add/i);
    
    fireEvent.change(nameInput, { target: { value: "Bread" } });
    fireEvent.click(submitButton);
    
    expect(mockOnAdd).toHaveBeenCalledWith(
      expect.objectContaining({
        name: "Bread"
      })
    );
  });

  it("should close modal after successful submission", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    const submitButton = screen.getByText(/add/i);
    
    fireEvent.change(nameInput, { target: { value: "Cheese" } });
    fireEvent.click(submitButton);
    
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("should prevent negative quantity values", () => {
    render(<AddItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const qtyInput = screen.getByLabelText(/quantity/i);
    
    expect(qtyInput).toHaveAttribute("min", "0");
  });
});

describe("EditItemModal Component", () => {
  const mockOnClose = jest.fn();
  const mockOnSave = jest.fn();
  
  const mockItem = {
    id: 1,
    name: "Milk",
    qty: 2,
    expiration: "2025-10-30"
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render the modal", () => {
    render(
      <EditItemModal 
        item={mockItem} 
        onClose={mockOnClose} 
        onSave={mockOnSave} 
      />
    );
    
    expect(screen.getByText(/edit item/i)).toBeInTheDocument();
  });

  it("should pre-fill form with item data", () => {
    render(
      <EditItemModal 
        item={mockItem} 
        onClose={mockOnClose} 
        onSave={mockOnSave} 
      />
    );
    
    const nameInput = screen.getByLabelText(/name/i);
    const qtyInput = screen.getByLabelText(/quantity/i);
    const expirationInput = screen.getByLabelText(/expiration/i);
    
    expect(nameInput).toHaveValue("Milk");
    expect(qtyInput).toHaveValue(2);
    expect(expirationInput).toHaveValue("2025-10-30");
  });

  it("should call onClose when cancel button clicked", () => {
    render(
      <EditItemModal 
        item={mockItem} 
        onClose={mockOnClose} 
        onSave={mockOnSave} 
      />
    );
    
    const cancelButton = screen.getByText(/cancel/i);
    fireEvent.click(cancelButton);
    
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("should call onSave with updated data when submitted", () => {
    render(
      <EditItemModal 
        item={mockItem} 
        onClose={mockOnClose} 
        onSave={mockOnSave} 
      />
    );
    
    const nameInput = screen.getByLabelText(/name/i);
    const qtyInput = screen.getByLabelText(/quantity/i);
    const saveButton = screen.getByText(/save/i);
    
    fireEvent.change(nameInput, { target: { value: "Whole Milk" } });
    fireEvent.change(qtyInput, { target: { value: "3" } });
    fireEvent.click(saveButton);
    
    expect(mockOnSave).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 1,
        name: "Whole Milk",
        qty: 3
      })
    );
  });

  it("should preserve item ID when saving", () => {
    render(
      <EditItemModal 
        item={mockItem} 
        onClose={mockOnClose} 
        onSave={mockOnSave} 
      />
    );
    
    const saveButton = screen.getByText(/save/i);
    fireEvent.click(saveButton);
    
    expect(mockOnSave).toHaveBeenCalledWith(
      expect.objectContaining({ id: 1 })
    );
  });

  it("should close modal after successful save", () => {
    render(
      <EditItemModal 
        item={mockItem} 
        onClose={mockOnClose} 
        onSave={mockOnSave} 
      />
    );
    
    const saveButton = screen.getByText(/save/i);
    fireEvent.click(saveButton);
    
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("should handle item without expiration date", () => {
    const itemWithoutExpiration = {
      id: 2,
      name: "Salt",
      qty: 1,
      expiration: null
    };
    
    render(
      <EditItemModal 
        item={itemWithoutExpiration} 
        onClose={mockOnClose} 
        onSave={mockOnSave} 
      />
    );
    
    const expirationInput = screen.getByLabelText(/expiration/i);
    expect(expirationInput).toHaveValue("");
  });
});

describe("AddShoppingItemModal Component", () => {
  const mockOnClose = jest.fn();
  const mockOnAdd = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render the modal", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    expect(screen.getByText(/add.*shopping.*item/i)).toBeInTheDocument();
  });

  it("should have name input field", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    expect(nameInput).toBeInTheDocument();
    expect(nameInput).toHaveAttribute("required");
  });

  it("should have quantity input field", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const qtyInput = screen.getByLabelText(/quantity/i);
    expect(qtyInput).toBeInTheDocument();
    expect(qtyInput).toHaveAttribute("type", "number");
  });

  it("should NOT have expiration date field", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const expirationInput = screen.queryByLabelText(/expiration/i);
    expect(expirationInput).not.toBeInTheDocument();
  });

  it("should call onClose when cancel button clicked", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const cancelButton = screen.getByText(/cancel/i);
    fireEvent.click(cancelButton);
    
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("should call onAdd with form data when submitted", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    const qtyInput = screen.getByLabelText(/quantity/i);
    const submitButton = screen.getByText(/add/i);
    
    fireEvent.change(nameInput, { target: { value: "Apples" } });
    fireEvent.change(qtyInput, { target: { value: "5" } });
    fireEvent.click(submitButton);
    
    expect(mockOnAdd).toHaveBeenCalledWith({
      name: "Apples",
      qty: 5
    });
  });

  it("should not submit without required name field", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const submitButton = screen.getByText(/add/i);
    fireEvent.click(submitButton);
    
    expect(mockOnAdd).not.toHaveBeenCalled();
  });

  it("should submit with only name field", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    const submitButton = screen.getByText(/add/i);
    
    fireEvent.change(nameInput, { target: { value: "Bananas" } });
    fireEvent.click(submitButton);
    
    expect(mockOnAdd).toHaveBeenCalledWith(
      expect.objectContaining({
        name: "Bananas"
      })
    );
  });

  it("should close modal after successful submission", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const nameInput = screen.getByLabelText(/name/i);
    const submitButton = screen.getByText(/add/i);
    
    fireEvent.change(nameInput, { target: { value: "Oranges" } });
    fireEvent.click(submitButton);
    
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("should prevent negative quantity values", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const qtyInput = screen.getByLabelText(/quantity/i);
    
    expect(qtyInput).toHaveAttribute("min", "0");
  });

  it("should have default quantity of 1", () => {
    render(<AddShoppingItemModal onClose={mockOnClose} onAdd={mockOnAdd} />);
    
    const qtyInput = screen.getByLabelText(/quantity/i);
    
    expect(qtyInput).toHaveValue(1);
  });
});