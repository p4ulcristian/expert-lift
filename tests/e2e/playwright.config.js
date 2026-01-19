// @ts-check
import { defineConfig } from '@playwright/test';
import dotenv from 'dotenv';

// Load .env.local from project root
dotenv.config({ path: '../../.env.local' });

const VIEWPORT = { width: 1280, height: 720 };

export default defineConfig({
  testDir: './',
  outputDir: './test-results',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  timeout: 300000,

  expect: {
    timeout: 10000,
  },

  use: {
    baseURL: process.env.BASE_URL || 'https://localhost',
    viewport: VIEWPORT,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'off',
    ignoreHTTPSErrors: true,
    actionTimeout: 15000,
    navigationTimeout: 30000,
  },

  projects: [
    // Auth setup - runs once, saves session
    {
      name: 'setup',
      testMatch: /auth\.setup\.js/,
    },
    // Main tests - use saved session
    {
      name: 'chromium',
      dependencies: ['setup'],
      use: {
        // Load saved auth state
        storageState: '.auth/user.json',
        launchOptions: {
          args: [
            // Memory & stability
            '--disable-dev-shm-usage',
            '--disable-extensions',

            // Prevent background throttling
            '--disable-background-timer-throttling',
            '--disable-backgrounding-occluded-windows',
            '--disable-renderer-backgrounding',

            // GPU & WebGL
            '--ignore-gpu-blocklist',
            '--enable-webgl',
            '--enable-webgl2',
            ...(process.env.CI ? [
              '--disable-gpu',
              '--use-gl=swiftshader',
            ] : [
              '--enable-gpu-rasterization',
              '--enable-zero-copy',
              '--use-gl=egl',
            ]),

            // Reduce unnecessary features
            '--disable-translate',
            '--disable-sync',
            '--disable-default-apps',
            '--no-first-run',
            '--disable-popup-blocking',
          ],
        },
      },
    },
  ],
});
