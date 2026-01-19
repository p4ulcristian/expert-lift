/**
 * Auth Setup - Playwright authentication setup project
 *
 * Handles browser-based login for Elevathor
 *
 * Required env vars:
 *   ELEVATHOR_E2E_EMAIL - test user email (unused, kept for compatibility)
 *   ELEVATHOR_E2E_PASSWORD - test user password
 *   ELEVATHOR_E2E_WORKSPACE_ID - workspace ID for testing
 */

import { test as setup, expect } from '@playwright/test';
import * as fs from 'fs';
import * as dotenv from 'dotenv';

// Load env vars from .env.local
dotenv.config({ path: '.env.local' });

// Auth state file
const AUTH_FILE = '.auth/user.json';

setup('authenticate', async ({ page }) => {
  // Using username 'valentin' for login
  const username = 'valentin';
  const password = process.env.ELEVATHOR_E2E_PASSWORD;
  const workspaceId = process.env.ELEVATHOR_E2E_WORKSPACE_ID;
  const baseUrl = process.env.BASE_URL || 'http://localhost:3000';

  if (!password) {
    throw new Error('ELEVATHOR_E2E_PASSWORD is required in .env.local');
  }

  if (!workspaceId) {
    throw new Error('ELEVATHOR_E2E_WORKSPACE_ID is required in .env.local');
  }

  console.log(`\n🔐 Logging in as: ${username}`);
  console.log(`📡 Base URL: ${baseUrl}`);

  // Navigate to login page
  await page.goto(`${baseUrl}/login`);
  await page.waitForLoadState('load');

  const currentUrl = page.url();
  console.log('📍 Current URL:', currentUrl);

  // Check if we're on the login page
  if (currentUrl.includes('/login')) {
    console.log('🔑 On login page, filling credentials...');

    // Fill username
    const usernameInput = page.locator('input[name="username"]');
    await usernameInput.waitFor({ timeout: 10000 });
    await usernameInput.fill(username);

    // Fill password
    const passwordInput = page.locator('input[name="password"]');
    await passwordInput.waitFor({ timeout: 3000 });
    await passwordInput.fill(password);

    // Click submit button
    const submitButton = page.locator('button[type="submit"]');
    await submitButton.click();

    // Wait for redirect to app
    await page.waitForURL(url => {
      const urlStr = url.toString();
      return urlStr.includes('/app/') || urlStr.includes('/superadmin');
    }, { timeout: 30000 });

    await page.waitForLoadState('load', { timeout: 10000 });
    await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});

    console.log('✅ Login successful');
  } else if (currentUrl.includes('/app/')) {
    console.log('✅ Already authenticated (session exists)');
  } else {
    console.log(`⚠️ Unexpected URL after navigation: ${currentUrl}`);
  }

  // Debug: check current URL and title
  console.log('📍 Current URL:', page.url());
  const pageTitle = await page.title();
  console.log('📄 Page title:', pageTitle);

  // Ensure .auth directory exists
  fs.mkdirSync('.auth', { recursive: true });

  // Save auth state
  await page.context().storageState({ path: AUTH_FILE });
  console.log(`💾 Session saved to ${AUTH_FILE}\n`);
});
