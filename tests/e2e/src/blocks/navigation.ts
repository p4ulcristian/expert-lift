/**
 * Navigation blocks for Elevathor
 */

import type { Page } from '@playwright/test';

/**
 * Navigate to entity list page.
 * Elevathor routes: /app/:workspace-id/:entity-plural
 */
export async function goToList(page: Page, workspaceId: string, entityPlural: string): Promise<void> {
  await page.goto(`/app/${workspaceId}/${entityPlural}`);
  await page.waitForLoadState('load');
}

/**
 * Wait for form/modal to be visible.
 * In Elevathor, forms are typically modals.
 */
export async function waitForModal(page: Page): Promise<void> {
  const modal = page.locator('[data-testid="modal"], .modal-content, [role="dialog"]').first();
  await modal.waitFor({ state: 'visible', timeout: 10000 });
}

/**
 * Wait for modal to be closed.
 */
export async function waitForModalClosed(page: Page): Promise<void> {
  const modal = page.locator('[data-testid="modal"], .modal-content, [role="dialog"]').first();
  await modal.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
}

/**
 * Navigate to material templates list
 */
export async function goToMaterialTemplates(page: Page, workspaceId: string): Promise<void> {
  await goToList(page, workspaceId, 'material-templates');
}

/**
 * Navigate to addresses list
 */
export async function goToAddresses(page: Page, workspaceId: string): Promise<void> {
  await goToList(page, workspaceId, 'addresses');
}

/**
 * Navigate to worksheets list
 */
export async function goToWorksheets(page: Page, workspaceId: string): Promise<void> {
  await goToList(page, workspaceId, 'worksheets');
}
