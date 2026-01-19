/**
 * Module: workspace.login
 *
 * Authenticates user and navigates to app workspace page.
 */
import * as b from '../../blocks/index.js';

export default {
  name: 'workspace.login',
  description: 'Authenticate and navigate to app workspace',

  requires: ['page'],
  produces: ['authenticated', 'workspaceId'],

  async run(context) {
    const { page, config = {} } = context;
    const email = config.email || b.TEST_USER.email;
    const password = config.password || b.TEST_USER.password;
    const workspaceId = config.workspaceId || b.TEST_WORKSPACE_ID;

    if (!workspaceId) {
      throw new Error('ELEVATHOR_E2E_WORKSPACE_ID environment variable is required');
    }

    // Navigate to app
    await page.goto(`/app/${workspaceId}`);
    await page.waitForLoadState('load');

    // Check if we're already authenticated
    let currentUrl = page.url();
    if (currentUrl.includes('/app/') && !currentUrl.includes('/login') && !currentUrl.includes('auth0.com')) {
      // Already authenticated
      await page.waitForLoadState('networkidle').catch(() => {});
      return { authenticated: true, workspaceId };
    }

    // Need to login via Auth0
    if (currentUrl.includes('auth0.com') || currentUrl.includes('/login')) {
      const loginSuccess = await b.login(page, email, password);
      if (!loginSuccess) {
        throw new Error('Login failed');
      }

      // Wait for redirect to app after login
      await page.waitForURL(url => {
        const path = new URL(url).pathname;
        return path.startsWith('/app/') && !path.includes('/login');
      }, { timeout: 30000 });
    }

    // Verify we're on app page
    if (!page.url().includes('/app/')) {
      throw new Error('Not on app page after authentication');
    }

    // Wait for page to stabilize
    await page.waitForLoadState('networkidle').catch(() => {});

    return { authenticated: true, workspaceId };
  },
};
