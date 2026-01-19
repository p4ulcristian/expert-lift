import type { Page } from '@playwright/test';
import { locator } from './locator';

export async function exists(page: Page, testId: string): Promise<boolean> {
  const el = locator(page, testId);
  return (await el.count()) > 0;
}
