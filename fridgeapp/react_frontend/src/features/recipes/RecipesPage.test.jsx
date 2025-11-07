import React from "react";
import { render, screen } from "@testing-library/react";
import RecipesPage from "./RecipesPage";

describe("RecipesPage Component", () => {
  const defaultProps = {
    recipes: [
      { id: 1, name: "Recipe 1", description: "Desc 1", cookTime: 30, servings: 4 },
      { id: 2, name: "Recipe 2", description: "Desc 2", cookTime: 45, servings: 6 }
    ],
    fridgeItems: [],
    reload: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render the recipes page", () => {
    render(<RecipesPage {...defaultProps} />);
    expect(screen.getByText("Recipes")).toBeInTheDocument();
  });

  it("should display recipe cards", () => {
    render(<RecipesPage {...defaultProps} />);
    expect(screen.getByText("Recipe 1")).toBeInTheDocument();
    expect(screen.getByText("Recipe 2")).toBeInTheDocument();
  });

  it("should have search input", () => {
    render(<RecipesPage {...defaultProps} />);
    const searchInput = screen.getByPlaceholderText(/search/i);
    expect(searchInput).toBeInTheDocument();
  });
});