import React from "react";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import Sidebar from "./Sidebar";

describe("Sidebar Component", () => {
  const renderSidebar = () => {
    return render(
      <BrowserRouter>
        <Sidebar />
      </BrowserRouter>
    );
  };

  it("should render FridgeApp title", () => {
    renderSidebar();
    expect(screen.getByText("FridgeApp")).toBeInTheDocument();
  });

  it("should render navigation links", () => {
    renderSidebar();
    expect(screen.getByText("Fridge")).toBeInTheDocument();
    expect(screen.getByText("Shopping List")).toBeInTheDocument();
    expect(screen.getByText("Recipes")).toBeInTheDocument();
  });

  it("should have nav element", () => {
    const { container } = renderSidebar();
    expect(container.querySelector("nav")).toBeInTheDocument();
  });
});