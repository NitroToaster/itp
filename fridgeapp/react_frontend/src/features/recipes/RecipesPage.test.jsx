import React from "react";
import { render, screen } from "@testing-library/react";
import RecipesPage from "./RecipesPage";

describe("RecipesPage Component", () => {
  const defaultProps = {
    recipes: [],
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

  it("should have search input", () => {
    render(<RecipesPage {...defaultProps} />);
    const searchInput = screen.getByPlaceholderText(/search/i);
    expect(searchInput).toBeInTheDocument();
  });

  it("should have Clear button", () => {
    render(<RecipesPage {...defaultProps} />);
    expect(screen.getByText("Clear")).toBeInTheDocument();
  });

  it("should have Mixed button", () => {
    render(<RecipesPage {...defaultProps} />);
    expect(screen.getByText("Mixed")).toBeInTheDocument();
  });

  it("should have Favorites button", () => {
    render(<RecipesPage {...defaultProps} />);
    expect(screen.getByText(/Favorites/i)).toBeInTheDocument();
  });

  it("should show loading state", () => {
    render(<RecipesPage {...defaultProps} />);
    expect(screen.getByText("Loading recipes...")).toBeInTheDocument();
  });

  it("should show loading categories text", () => {
    render(<RecipesPage {...defaultProps} />);
    expect(screen.getByText("Loading categories...")).toBeInTheDocument();
  });

  it("should render panel", () => {
    const { container } = render(<RecipesPage {...defaultProps} />);
    expect(container.querySelector(".panel")).toBeInTheDocument();
  });
});