import type { Page } from '@playwright/test';
import { TIMEOUT } from './timeout';
import { locator } from './locator';

export async function fill(page: Page, testId: string, value: string) {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  await el.scrollIntoViewIfNeeded();
  await el.evaluate((input, val) => {
    const el = input as HTMLInputElement;
    el.focus();
    const setter = Object.getOwnPropertyDescriptor(
      window.HTMLInputElement.prototype,
      'value'
    )?.set;
    setter?.call(el, val);
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
    el.blur();
  }, value);
}

export async function fillTextarea(page: Page, testId: string, value: string) {
  const el = locator(page, testId);
  await el.waitFor({ state: 'visible', timeout: TIMEOUT });
  await el.scrollIntoViewIfNeeded();
  await el.evaluate((textarea, val) => {
    const el = textarea as HTMLTextAreaElement;
    el.focus();
    const setter = Object.getOwnPropertyDescriptor(
      window.HTMLTextAreaElement.prototype,
      'value'
    )?.set;
    setter?.call(el, val);
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
    el.blur();
  }, value);
}
