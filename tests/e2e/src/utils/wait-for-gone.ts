import type { Page } from '@playwright/test';
import { TIMEOUT } from './timeout';
import { locator } from './locator';

export async function waitForGone(page: Page, testId: string) {
  const el = locator(page, testId);
  await el.waitFor({ state: 'hidden', timeout: TIMEOUT });
}
