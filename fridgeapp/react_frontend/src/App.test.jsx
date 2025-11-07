import React from "react";
import { render, screen } from "@testing-library/react";
import App from "./App";

describe("App Component", () => {
  it("should render without crashing", () => {
    render(<App />);
    expect(screen.getByText("FridgeApp")).toBeInTheDocument();
  });

  it("should render sidebar", () => {
    render(<App />);
    expect(screen.getByText("Fridge Overview")).toBeInTheDocument();
    expect(screen.getByText("Shopping List")).toBeInTheDocument();
    expect(screen.getByText("Recipes")).toBeInTheDocument();
  });

  it("should render workspace", () => {
    const { container } = render(<App />);
    expect(container.querySelector(".workspace")).toBeInTheDocument();
  });
});