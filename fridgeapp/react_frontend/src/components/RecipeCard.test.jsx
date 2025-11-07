import React from "react";
import { render, screen } from "@testing-library/react";
import RecipeCard from "./RecipeCard";

describe("RecipeCard Component", () => {
  const mockRecipe = {
    id: 1,
    name: "Test Recipe",
    description: "Test Description",
    cookTime: 30,
    servings: 4,
    ingredients: ["ingredient1", "ingredient2"]
  };

  it("should render recipe card", () => {
    const { container } = render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(container.querySelector(".recipe-card")).toBeInTheDocument();
  });

  it("should display ingredients count", () => {
    render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(screen.getByText(/2.*ingredients/i)).toBeInTheDocument();
  });

  it("should have favorite button", () => {
    render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(screen.getByText("🤍")).toBeInTheDocument();
  });

  it("should render with no image placeholder", () => {
    render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(screen.getByText("No image")).toBeInTheDocument();
  });

  it("should be clickable", () => {
    const mockClick = jest.fn();
    const { container } = render(<RecipeCard recipe={mockRecipe} onClick={mockClick} />);
    const card = container.querySelector(".recipe-card");
    expect(card).toHaveStyle("cursor: pointer");
  });
});