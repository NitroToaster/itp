// testUtils.js
// Shared test utilities and helpers

import { render } from "@testing-library/react";
import React from "react";

/**
 * Custom render function with common providers
 * Extend this as needed with context providers, routers, etc.
 */
export function renderWithProviders(ui, options = {}) {
  const Wrapper = ({ children }) => {
    return <>{children}</>;
  };

  return render(ui, { wrapper: Wrapper, ...options });
}

/**
 * Mock API response helper
 */
export function mockApiResponse(data, delay = 0) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ data });
    }, delay);
  });
}

/**
 * Mock API error helper
 */
export function mockApiError(message = "API Error", delay = 0) {
  return new Promise((_, reject) => {
    setTimeout(() => {
      reject(new Error(message));
    }, delay);
  });
}

/**
 * Create mock fridge item
 */
export function createMockFridgeItem(overrides = {}) {
  return {
    id: Math.floor(Math.random() * 10000),
    name: "Test Item",
    qty: 1,
    expiration: "2025-12-31",
    ...overrides,
  };
}

/**
 * Create mock shopping item
 */
export function createMockShoppingItem(overrides = {}) {
  return {
    id: Math.floor(Math.random() * 10000),
    name: "Test Shopping Item",
    qty: 1,
    ...overrides,
  };
}

/**
 * Create multiple mock items
 */
export function createMockItems(count, itemFactory) {
  return Array.from({ length: count }, (_, index) =>
    itemFactory({ id: index + 1 })
  );
}

/**
 * Wait for a condition to be true
 */
export function waitForCondition(condition, timeout = 3000) {
  return new Promise((resolve, reject) => {
    const startTime = Date.now();
    const interval = setInterval(() => {
      if (condition()) {
        clearInterval(interval);
        resolve();
      } else if (Date.now() - startTime > timeout) {
        clearInterval(interval);
        reject(new Error("Timeout waiting for condition"));
      }
    }, 100);
  });
}

/**
 * Create a mock File object for testing file uploads
 */
export function createMockFile(content, filename = "test.json", type = "application/json") {
  const blob = new Blob([JSON.stringify(content)], { type });
  const file = new File([blob], filename, { type });
  return file;
}

/**
 * Mock console methods for testing
 */
export function mockConsole() {
  const originalConsole = { ...console };
  
  beforeAll(() => {
    global.console = {
      ...console,
      log: jest.fn(),
      error: jest.fn(),
      warn: jest.fn(),
      info: jest.fn(),
      debug: jest.fn(),
    };
  });

  afterAll(() => {
    global.console = originalConsole;
  });

  return global.console;
}

/**
 * Create mock event
 */
export function createMockEvent(type, properties = {}) {
  const event = new Event(type);
  Object.assign(event, properties);
  return event;
}

/**
 * Flush all pending promises
 */
export function flushPromises() {
  return new Promise((resolve) => setImmediate(resolve));
}

/**
 * Assert that a function throws a specific error
 */
export async function expectAsyncError(asyncFn, errorMessage) {
  try {
    await asyncFn();
    throw new Error("Expected function to throw an error");
  } catch (error) {
    if (errorMessage) {
      expect(error.message).toContain(errorMessage);
    }
  }
}

/**
 * Get date relative to today
 */
export function getRelativeDate(daysFromNow) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split("T")[0];
}

/**
 * Create expired item
 */
export function createExpiredItem(overrides = {}) {
  return createMockFridgeItem({
    name: "Expired Item",
    expiration: getRelativeDate(-5),
    ...overrides,
  });
}

/**
 * Create expiring soon item
 */
export function createExpiringSoonItem(overrides = {}) {
  return createMockFridgeItem({
    name: "Expiring Soon Item",
    expiration: getRelativeDate(2),
    ...overrides,
  });
}

/**
 * Create fresh item
 */
export function createFreshItem(overrides = {}) {
  return createMockFridgeItem({
    name: "Fresh Item",
    expiration: getRelativeDate(30),
    ...overrides,
  });
}

/**
 * Mock localStorage with initial data
 */
export function mockLocalStorageWithData(fridgeItems = [], shoppingItems = []) {
  localStorage.setItem("fridge_mock_store_v1", JSON.stringify(fridgeItems));
  localStorage.setItem("shopping_mock_store_v1", JSON.stringify(shoppingItems));
}

/**
 * Get all localStorage keys
 */
export function getLocalStorageKeys() {
  return Object.keys(localStorage);
}

/**
 * Clear all localStorage
 */
export function clearLocalStorage() {
  localStorage.clear();
}

/**
 * Create mock axios instance
 */
export function createMockAxios() {
  return {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    create: jest.fn(() => createMockAxios()),
    defaults: {
      baseURL: "http://localhost:8080/api/v1",
      headers: {
        "Content-Type": "application/json",
      },
    },
  };
}

/**
 * Wait for element to appear
 */
export async function waitForElement(getElement, timeout = 3000) {
  const startTime = Date.now();
  while (Date.now() - startTime < timeout) {
    try {
      const element = getElement();
      if (element) return element;
    } catch (error) {
      // Element not found yet, continue waiting
    }
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
  throw new Error("Element not found within timeout");
}

/**
 * Simulate user typing with delay between keystrokes
 */
export async function typeWithDelay(element, text, delayMs = 10) {
  for (const char of text) {
    element.value += char;
    element.dispatchEvent(new Event("input", { bubbles: true }));
    await new Promise((resolve) => setTimeout(resolve, delayMs));
  }
}

/**
 * Create mock component props
 */
export function createMockProps(overrides = {}) {
  return {
    items: [],
    loading: false,
    onAdd: jest.fn(),
    onRemove: jest.fn(),
    onUpdate: jest.fn(),
    reload: jest.fn(),
    ...overrides,
  };
}

// Export commonly used test data
export const TEST_DATA = {
  fridgeItem: {
    id: 1,
    name: "Milk",
    qty: 2,
    expiration: "2025-12-31",
  },
  shoppingItem: {
    id: 1,
    name: "Bread",
    qty: 1,
  },
  importData: {
    fridgeItems: [
      { id: 1, name: "Cheese", qty: 1, expiration: "2025-11-30" },
      { id: 2, name: "Eggs", qty: 12, expiration: "2025-11-15" },
    ],
    shoppingItems: [
      { id: 1, name: "Butter", qty: 2 },
      { id: 2, name: "Flour", qty: 1 },
    ],
  },
};