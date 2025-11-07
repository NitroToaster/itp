import React from "react";
import { render, screen } from "@testing-library/react";
import RecipeCard from "./RecipeCard";

describe("RecipeCard Component", () => {
  const mockRecipe = {
    id: 1,
    name: "Test Recipe",
    description: "Test Description",
    cookTime: 30,
    servings: 4
  };

  it("should render recipe name", () => {
    render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(screen.getByText("Test Recipe")).toBeInTheDocument();
  });

  it("should render recipe description", () => {
    render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(screen.getByText("Test Description")).toBeInTheDocument();
  });

  it("should display cook time", () => {
    render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(screen.getByText(/30/)).toBeInTheDocument();
  });

  it("should display servings", () => {
    render(<RecipeCard recipe={mockRecipe} onClick={() => {}} />);
    expect(screen.getByText(/4/)).toBeInTheDocument();
  });
});