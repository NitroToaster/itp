import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import Sidebar from "./Sidebar";

describe("Sidebar Component", () => {
  it("should render FridgeApp title", () => {
    render(<Sidebar />);
    expect(screen.getByText("FridgeApp")).toBeInTheDocument();
  });

  it("should render subtitle", () => {
    render(<Sidebar />);
    expect(screen.getByText("Kitchen command center")).toBeInTheDocument();
  });

  it("should render navigation links", () => {
    render(<Sidebar />);
    expect(screen.getByText("Fridge Overview")).toBeInTheDocument();
    expect(screen.getByText("Shopping List")).toBeInTheDocument();
    expect(screen.getByText("Recipes")).toBeInTheDocument();
  });

  it("should render Load Data button", () => {
    render(<Sidebar />);
    expect(screen.getByText("Load Data")).toBeInTheDocument();
  });

  it("should render footer text", () => {
    render(<Sidebar />);
    expect(screen.getByText("Groceries made simple")).toBeInTheDocument();
  });

  it("should have nav element", () => {
    const { container } = render(<Sidebar />);
    expect(container.querySelector("nav")).toBeInTheDocument();
  });

  it("should have sidebar toggle button", () => {
    const { container } = render(<Sidebar />);
    const toggleBtn = container.querySelector(".sidebar-toggle-btn");
    expect(toggleBtn).toBeInTheDocument();
  });

  it("should have Overview section", () => {
    render(<Sidebar />);
    expect(screen.getByText("Overview")).toBeInTheDocument();
  });

  it("should have Data section", () => {
    render(<Sidebar />);
    expect(screen.getByText("Data")).toBeInTheDocument();
  });
});