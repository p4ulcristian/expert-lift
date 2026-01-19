import type { Page } from '@playwright/test';
import { TIMEOUT } from './timeout';
import { locator } from './locator';

export async function getText(page: Page, testId: string): Promise<string> {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  return await el.textContent() || '';
}
