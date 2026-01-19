import type { Page } from '@playwright/test';
import { TIMEOUT } from './timeout';
import { locator } from './locator';

export async function select(page: Page, testId: string, value: string) {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  await el.scrollIntoViewIfNeeded();
  await el.selectOption(value);
}
