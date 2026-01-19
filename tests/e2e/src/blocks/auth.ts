/**
 * Authentication blocks for Elevathor
 */

import type { Page } from '@playwright/test';
import { TEST_USER } from '../dummy-data';

interface LoginOptions {
  email?: string;
  password?: string;
}

/**
 * Login with email and password (handles Auth0 flow)
 */
export async function login(page: Page, email: string, password: string, timeout = 30000): Promise<boolean> {
  try {
    const isAuth0 = page.url().includes('auth0.com') || page.url().includes('/login/');

    if (isAuth0) {
      const emailInput = page.locator('input[name="email"], input[name="username"], input[type="email"]').first();
      await emailInput.waitFor({ timeout });
      await emailInput.fill(email);

      const passwordInput = page.locator('input[name="password"], input[type="password"]').first();
      await passwordInput.waitFor({ timeout: 3000 });
      await passwordInput.fill(password);

      const continueButton = page.locator('button:has-text("Continue"), button[type="submit"]').first();
      if (await continueButton.count() > 0) {
        await continueButton.click({ noWaitAfter: true });
      } else {
        await passwordInput.press('Enter', { noWaitAfter: true });
      }

      await page.waitForURL(url => {
        const urlStr = url.toString();
        return !urlStr.includes('auth0.com') && !urlStr.includes('/login/');
      }, { timeout: 30000 });

      await page.waitForLoadState('load', { timeout: 10000 });
      await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});

      return true;
    }

    const loginButton = page.locator('button:has-text("Login"), button:has-text("Sign in")').first();
    if (await loginButton.count() > 0) {
      await loginButton.click();
    }

    // Prefer data-testid, fall back to other selectors
    const emailInput = page.locator('[data-testid="login-username-input"], input[type="email"], input[name="email"], input[placeholder*="email" i]').first();
    await emailInput.waitFor({ timeout });
    await emailInput.fill(email);

    const passwordInput = page.locator('[data-testid="login-password-input"], input[type="password"], input[name="password"]').first();
    await passwordInput.waitFor({ timeout: 3000 });
    await passwordInput.fill(password);

    const submitButton = page.locator('[data-testid="login-submit-button"], button[type="submit"], button:has-text("Login"), button:has-text("Sign in"), button:has-text("Continue")').first();
    await submitButton.click();

    await page.waitForLoadState('networkidle', { timeout });
    return true;
  } catch (error) {
    console.log(`Login failed: ${(error as Error).message}`);
    return false;
  }
}

/**
 * Perform authentication if needed
 */
export async function ensureAuthenticated(page: Page, options: LoginOptions = {}): Promise<boolean> {
  const { email = TEST_USER.email, password = TEST_USER.password } = options;

  if (!page.url().includes('/app')) {
    await page.goto('/app');
    await page.waitForLoadState('load');
  }

  if (page.url().includes('auth0.com') || page.url().includes('/login/')) {
    const loginSuccess = await login(page, email, password);
    if (!loginSuccess) {
      throw new Error('Login failed');
    }
  }

  if (!page.url().includes('/app')) {
    throw new Error('Not on app page after authentication');
  }

  return true;
}

/**
 * Wait for authenticated app UI to be ready
 */
export async function waitForAppReady(page: Page): Promise<void> {
  // Wait for sidebar or main content to be visible
  const sidebar = page.locator('[data-testid="app-sidebar"], .sidebar, nav').first();
  await sidebar.waitFor({ state: 'visible', timeout: 15000 });
}

/**
 * Full authentication flow with UI ready check
 */
export async function authenticateAndWaitReady(page: Page, options: LoginOptions = {}): Promise<void> {
  await ensureAuthenticated(page, options);
  await waitForAppReady(page);
}
