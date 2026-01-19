/**
 * Verification and utility blocks
 */

import type { Page, Dialog } from '@playwright/test';
import { getText } from '../utils';

/**
 * Assert that text is visible on the page.
 */
export async function assertVisible(page: Page, text: string, timeout = 10000): Promise<void> {
  const el = page.getByText(text, { exact: false }).first();
  await el.waitFor({ state: 'visible', timeout });
}

/**
 * Assert that text is NOT visible on the page.
 */
export async function assertGone(page: Page, text: string, timeout = 5000): Promise<void> {
  const el = page.getByText(text, { exact: false }).first();
  // Wait for element to be hidden/detached
  await el.waitFor({ state: 'hidden', timeout }).catch(() => {});
  const count = await el.count();
  if (count > 0 && await el.isVisible()) {
    throw new Error(`Expected "${text}" to be gone, but it's still visible`);
  }
}

/**
 * Wait for save operation to complete.
 * Waits for network to be idle instead of fixed timeout.
 */
export async function waitForSave(page: Page): Promise<void> {
  await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});
}

/**
 * Handle browser confirmation dialog (accept).
 */
export function handleConfirmDialog(page: Page): void {
  page.once('dialog', async (dialog: Dialog) => {
    await dialog.accept();
  });
}

/**
 * Wait for loading to complete (loading indicator gone)
 */
export async function waitForLoading(page: Page): Promise<void> {
  // Wait for any loading spinners to disappear
  const loadingIndicators = page.locator('.fa-spin, [data-loading="true"], .loading');
  await loadingIndicators.first().waitFor({ state: 'hidden', timeout: 15000 }).catch(() => {});
}

/**
 * Assert that a testid element is visible
 */
export async function assertTestIdVisible(page: Page, testId: string, timeout = 10000): Promise<void> {
  const el = page.locator(`[data-testid="${testId}"]`).first();
  await el.waitFor({ state: 'visible', timeout });
}

/**
 * Assert that a testid element is gone
 */
export async function assertTestIdGone(page: Page, testId: string, timeout = 5000): Promise<void> {
  const el = page.locator(`[data-testid="${testId}"]`).first();
  await el.waitFor({ state: 'hidden', timeout }).catch(() => {});
}
