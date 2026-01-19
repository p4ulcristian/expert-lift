import type { Page, Locator } from '@playwright/test';

export function locator(page: Page, testId: string): Locator {
  return page.locator(`[data-testid="${testId}"]`);
}
