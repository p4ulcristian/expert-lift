/**
 * E2E Test Blocks - Central export
 *
 * Import all blocks with: import * as b from '../blocks/index.js';
 * Then use: b.login(), b.goToList(), etc.
 */

// Table blocks
export * from './table.js';

// Navigation blocks
export * from './navigation.js';

// Verification blocks
export * from './verification.js';

// Authentication blocks
export * from './auth.js';

// Form blocks
export * from './form.js';

// Test data and configuration
export * from '../dummy-data';

// Re-export utils for convenience
export * from '../utils/index.js';
