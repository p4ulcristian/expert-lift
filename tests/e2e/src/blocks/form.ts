/**
 * Form helper blocks for Elevathor
 */

import type { Page } from '@playwright/test';
import { TIMEOUT } from '../utils';
import { locator } from '../utils/locator';

/**
 * Fill an input field by test ID
 */
export async function fillInput(page: Page, testId: string, value: string): Promise<void> {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  await el.scrollIntoViewIfNeeded();
  await el.clear();
  await el.fill(value);
}

/**
 * Fill a textarea by test ID
 */
export async function fillTextarea(page: Page, testId: string, value: string): Promise<void> {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  await el.scrollIntoViewIfNeeded();
  await el.clear();
  await el.fill(value);
}

/**
 * Select an option from a dropdown by test ID
 */
export async function selectOption(page: Page, testId: string, value: string): Promise<void> {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  await el.scrollIntoViewIfNeeded();
  await el.selectOption(value);
}

/**
 * Click a button by test ID
 */
export async function clickButton(page: Page, testId: string): Promise<void> {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  await el.scrollIntoViewIfNeeded();
  await el.click();
}

/**
 * Fill address autocomplete field
 * This handles the address-search component which opens a dropdown
 */
export async function fillAddressAutocomplete(page: Page, testId: string, searchText: string): Promise<void> {
  const input = locator(page, testId);
  await input.waitFor({ state: 'visible', timeout: TIMEOUT });
  await input.scrollIntoViewIfNeeded();
  await input.fill(searchText);

  // Wait for dropdown to appear and click first option
  await page.waitForTimeout(500); // Allow dropdown to populate
  const firstOption = page.locator('[data-testid="address-option"]').first();
  if (await firstOption.count() > 0) {
    await firstOption.click();
  }
}

/**
 * Check if form field has error
 */
export async function hasFieldError(page: Page, testId: string): Promise<boolean> {
  const errorElement = page.locator(`[data-testid="${testId}-error"]`).first();
  return await errorElement.isVisible().catch(() => false);
}

/**
 * Get form field error message
 */
export async function getFieldError(page: Page, testId: string): Promise<string> {
  const errorElement = page.locator(`[data-testid="${testId}-error"]`).first();
  if (await errorElement.isVisible()) {
    return await errorElement.textContent() || '';
  }
  return '';
}
