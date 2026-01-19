/**
 * Unified Test Runner
 *
 * Auto-detects test type based on content:
 * - has "steps" → run as workflow
 * - has "workflows" → run as suite
 *
 * Usage: TEST=material-templates.crud bunx playwright test test.spec.js --headed
 */

import { test } from '@playwright/test';
import { loadSuite, runSuite, loadWorkflow, runWorkflow } from './runner.js';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const testName = process.env.TEST || 'full.crud-suite';

// Load and detect type
const testPath = path.join(__dirname, 'src', 'tests', `${testName}.json`);
const testData = JSON.parse(fs.readFileSync(testPath, 'utf-8'));
const isSuite = !!testData.workflows;

if (isSuite) {
  const suite = loadSuite(testName);

  test.describe(`${suite.name}`, () => {
    test(suite.description || 'Run test', async ({ page, request }) => {
      const result = await runSuite(page, request, suite);

      console.log('\n📋 Final context:');
      for (const [key, value] of Object.entries(result.context)) {
        if (!['page', 'request', 'expect', 'config'].includes(key)) {
          console.log(`   ${key}: ${typeof value === 'string' ? value : JSON.stringify(value)}`);
        }
      }
    });
  });
} else {
  const workflow = loadWorkflow(testName);

  const initialContext = {};
  if (process.env.WORKSPACE_ID) {
    initialContext.workspaceId = process.env.WORKSPACE_ID;
  }

  test.describe(`${workflow.name}`, () => {
    test(workflow.description, async ({ page, request }) => {
      const result = await runWorkflow(page, request, workflow, initialContext);

      console.log('\n📋 Final context:');
      for (const [key, value] of Object.entries(result)) {
        if (!['page', 'request', 'expect', 'config'].includes(key)) {
          console.log(`   ${key}: ${typeof value === 'string' ? value : JSON.stringify(value)}`);
        }
      }
    });
  });
}
