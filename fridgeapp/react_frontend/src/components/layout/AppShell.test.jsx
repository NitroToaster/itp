import React from "react";
import { render, screen } from "@testing-library/react";
import AppShell from "./AppShell";

describe("AppShell Component", () => {
  it("should render children", () => {
    render(
      <AppShell>
        <div>Test Content</div>
      </AppShell>
    );
    expect(screen.getByText("Test Content")).toBeInTheDocument();
  });

  it("should render with app-shell class", () => {
    const { container } = render(
      <AppShell>
        <div>Test</div>
      </AppShell>
    );
    expect(container.querySelector(".app-shell")).toBeInTheDocument();
  });
});