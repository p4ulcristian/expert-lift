/**
 * Data table blocks
 */

import type { Page, Locator } from '@playwright/test';
import { click, TIMEOUT } from '../utils';

/**
 * Find a row in data table by text content.
 */
export async function findRow(page: Page, text: string): Promise<Locator> {
  const row = page.locator(`[role="row"]:has-text("${text}")`).first();
  await row.waitFor({ timeout: TIMEOUT });
  return row;
}

/**
 * Click edit button in a data table row.
 * Uses data-testid pattern: edit-{entityName}-{entityId}
 */
export async function clickRowEdit(page: Page, entityName: string, entityId?: string): Promise<void> {
  if (entityId) {
    await click(page, `edit-${entityName}-${entityId}`);
  } else {
    // Fallback to class-based selector when no ID
    const btn = page.locator(`.edit-${entityName}-button`).first();
    await btn.waitFor({ timeout: TIMEOUT });
    await btn.click();
  }
}

/**
 * Click delete button in a data table row.
 * Uses data-testid pattern: delete-{entityName}-{entityId}
 */
export async function clickRowDelete(page: Page, entityName: string, entityId?: string): Promise<void> {
  if (entityId) {
    await click(page, `delete-${entityName}-${entityId}`);
  } else {
    // Fallback to class-based selector when no ID
    const btn = page.locator(`.delete-${entityName}-button`).first();
    await btn.waitFor({ timeout: TIMEOUT });
    await btn.click();
  }
}

/**
 * Wait for table to load (no loading spinner or loading text)
 */
export async function waitForTableLoaded(page: Page): Promise<void> {
  // Wait for various loading indicators to be hidden
  const loadingSpinner = page.locator('.fa-spin').first();
  await loadingSpinner.waitFor({ state: 'hidden', timeout: TIMEOUT }).catch(() => {});

  // Also wait for "Loading..." text to disappear
  const loadingText = page.getByText(/Loading|Betöltés/i).first();
  await loadingText.waitFor({ state: 'hidden', timeout: TIMEOUT }).catch(() => {});
}

/**
 * Get row count in the table
 */
export async function getRowCount(page: Page): Promise<number> {
  const rows = page.locator('[role="row"]');
  // Subtract 1 for header row
  const count = await rows.count();
  return Math.max(0, count - 1);
}
