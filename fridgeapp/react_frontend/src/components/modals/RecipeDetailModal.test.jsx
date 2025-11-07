import React from "react";
import { render, screen } from "@testing-library/react";
import RecipeDetailModal from "./RecipeDetailModal";

describe("RecipeDetailModal", () => {
  const mockRecipe = {
    id: 1,
    name: "Test Recipe",
    description: "Test Description",
    ingredients: ["ingredient1", "ingredient2"],
    instructions: ["step1", "step2"],
    cookTime: 30,
    servings: 4
  };

  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render modal", () => {
    const { container } = render(
      <RecipeDetailModal 
        recipe={mockRecipe} 
        onClose={mockOnClose}
        fridgeItems={[]}
      />
    );
    expect(container.querySelector(".modal")).toBeInTheDocument();
  });

  it("should have modal backdrop", () => {
    const { container } = render(
      <RecipeDetailModal 
        recipe={mockRecipe} 
        onClose={mockOnClose}
        fridgeItems={[]}
      />
    );
    expect(container.querySelector(".modal-backdrop")).toBeInTheDocument();
  });
});