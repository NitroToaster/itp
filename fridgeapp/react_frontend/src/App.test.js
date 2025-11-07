import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import App from "../App";

describe("App Integration Test", () => {
  it("renders FridgeApp title", () => {
    render(<App />);
    const titleElement = screen.getByText(/FridgeApp/i);
    expect(titleElement).toBeInTheDocument();
  });

  it("renders sidebar navigation", () => {
    render(<App />);
    expect(screen.getByText(/Fridge Overview/i)).toBeInTheDocument();
    expect(screen.getByText(/Shopping List/i)).toBeInTheDocument();
  });

  it("renders main workspace", () => {
    render(<App />);
    const workspace = screen.getByRole("main");
    expect(workspace).toBeInTheDocument();
  });
});