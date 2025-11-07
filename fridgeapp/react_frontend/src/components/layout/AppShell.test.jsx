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

  it("should render as a div", () => {
    const { container } = render(
      <AppShell>
        <div>Test</div>
      </AppShell>
    );
    expect(container.firstChild).toBeInTheDocument();
  });
});