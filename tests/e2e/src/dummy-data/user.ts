export interface TestUser {
  email: string;
  password: string;
}

// Credentials come from env vars - NEVER hardcode passwords!
// Local: .env.local file
// CI: GitHub Secrets
export const TEST_USER: TestUser = {
  email: process.env.ELEVATHOR_E2E_EMAIL || '',
  password: process.env.ELEVATHOR_E2E_PASSWORD || ''
};

// Default workspace ID for tests
export const TEST_WORKSPACE_ID = process.env.ELEVATHOR_E2E_WORKSPACE_ID || '';
