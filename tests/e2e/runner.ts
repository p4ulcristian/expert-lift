/**
 * Workflow Runner
 *
 * Executes workflow JSON files by loading and running modules in sequence.
 * Each module receives a context object and can add to it for subsequent modules.
 *
 * Supports:
 * - Workflows: Single sequence of modules
 * - Suites: Setup workflow + multiple child workflows sharing context
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { test, expect } from '@playwright/test';
import { getModule, validateWorkflow } from './src/modules/registry.ts';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

/**
 * Load a workflow from JSON file
 * @param {string} name - Workflow name (e.g., 'material-templates.crud')
 * @returns {Object} Workflow definition
 */
export function loadWorkflow(name) {
  const workflowPath = path.join(__dirname, 'src', 'tests', `${name}.json`);

  if (!fs.existsSync(workflowPath)) {
    const testsDir = path.join(__dirname, 'src', 'tests');
    const available = fs.readdirSync(testsDir)
      .filter(f => f.endsWith('.json'))
      .map(f => f.replace('.json', ''));
    throw new Error(`Workflow "${name}" not found. Available: ${available.join(', ')}`);
  }

  return JSON.parse(fs.readFileSync(workflowPath, 'utf-8'));
}

/**
 * List all available workflows
 * @returns {Array} List of workflow info
 */
export function listWorkflows() {
  const testsDir = path.join(__dirname, 'src', 'tests');
  return fs.readdirSync(testsDir)
    .filter(f => f.endsWith('.json'))
    .map(f => {
      const workflow = JSON.parse(fs.readFileSync(path.join(testsDir, f), 'utf-8'));
      return {
        name: f.replace('.json', ''),
        title: workflow.name,
        description: workflow.description,
        steps: workflow.steps?.length || 0,
        requires: workflow.requires || [],
      };
    });
}

/**
 * Run a single step with standardized logging
 * @param {Object} context - Current context
 * @param {Object} step - Step definition
 * @param {number} index - Step index (0-based)
 * @param {number} total - Total number of steps
 * @returns {Object} Step result with success status
 */
async function runStep(context, step, index, total) {
  const mod = getModule(step.module);
  const stepNum = index + 1;
  const stepName = `Step ${stepNum}/${total}: ${mod.name}`;

  console.log(`\n☐ ${stepName}`);

  const stepContext = {
    ...context,
    config: step.config || {},
  };

  try {
    const startTime = Date.now();
    const result = await mod.run(stepContext);
    const duration = ((Date.now() - startTime) / 1000).toFixed(1);

    console.log(`☑ ${stepName} (${duration}s)`);

    // Log produced values
    if (result) {
      for (const [key, value] of Object.entries(result)) {
        if (value !== undefined && value !== null) {
          console.log(`   → ${key}: ${typeof value === 'string' ? value : JSON.stringify(value)}`);
        }
      }
    }

    return { success: true, result };

  } catch (error) {
    console.log(`☒ ${stepName}`);
    console.log(`   Error: ${error.message}`);
    console.log(`\n${'═'.repeat(50)}`);
    console.log(`TEST STOPPED at step ${stepNum}/${total}`);
    console.log(`${'═'.repeat(50)}`);

    // Only pause for inspection if PAUSE_ON_ERROR is set
    if (process.env.PAUSE_ON_ERROR === 'true') {
      console.log(`Pausing for inspection (Playwright Inspector)...\n`);
      await context.page.pause();
    }

    throw error;
  }
}

/**
 * Run a workflow
 * @param {Object} page - Playwright page
 * @param {Object} request - Playwright API request context (for backend verification)
 * @param {Object} workflow - Workflow definition
 * @param {Object} initialContext - Initial context values (e.g., workspaceId)
 * @returns {Object} Final context after all steps
 */
export async function runWorkflow(page, request, workflow, initialContext = {}) {
  // Validate workflow before running (pass initialContext so it knows what's available)
  const validation = validateWorkflow(workflow, initialContext);
  if (!validation.valid) {
    throw new Error(`Invalid workflow: ${validation.errors.join(', ')}`);
  }

  // Check required initial context
  for (const req of workflow.requires || []) {
    if (!(req in initialContext)) {
      throw new Error(`Workflow requires "${req}" but it was not provided`);
    }
  }

  console.log(`\n${'═'.repeat(50)}`);
  console.log(`WORKFLOW: ${workflow.name}`);
  console.log(`${workflow.description}`);
  console.log(`${'═'.repeat(50)}`);

  // Initialize context with page, request, and initial values
  const context = {
    page,
    request,
    expect,
    ...initialContext,
  };

  const total = workflow.steps.length;
  const workflowStartTime = Date.now();
  const stepTimings: { name: string; duration: number }[] = [];

  // Run each step
  for (let i = 0; i < total; i++) {
    const step = workflow.steps[i];
    const stepStartTime = Date.now();
    const { result } = await runStep(context, step, i, total);
    const stepDuration = (Date.now() - stepStartTime) / 1000;

    stepTimings.push({ name: step.module, duration: stepDuration });

    // Add produced values to context
    if (result) {
      for (const [key, value] of Object.entries(result)) {
        context[key] = value;
      }
    }
  }

  const workflowDuration = ((Date.now() - workflowStartTime) / 1000).toFixed(1);

  console.log(`\n${'═'.repeat(50)}`);
  console.log(`✓ WORKFLOW COMPLETE: ${workflow.name} (${workflowDuration}s)`);
  console.log(`${'═'.repeat(50)}\n`);

  return { ...context, _workflowDuration: parseFloat(workflowDuration), _stepTimings: stepTimings };
}

/**
 * Create a Playwright test from a workflow
 * @param {string} workflowName - Name of the workflow file (without .json)
 * @param {Object} initialContext - Initial context to pass to workflow
 */
export function createWorkflowTest(workflowName, initialContext = {}) {
  const workflow = loadWorkflow(workflowName);

  test(workflow.name, async ({ page }) => {
    await runWorkflow(page, workflow, initialContext);
  });
}

// ============================================================================
// SUITE SUPPORT
// ============================================================================

/**
 * Load a suite from JSON file
 * @param {string} name - Suite name (e.g., 'full.crud')
 * @returns {Object} Suite definition
 */
export function loadSuite(name) {
  const suitePath = path.join(__dirname, 'src', 'tests', `${name}.json`);

  if (!fs.existsSync(suitePath)) {
    const testsDir = path.join(__dirname, 'src', 'tests');
    const available = fs.readdirSync(testsDir)
      .filter(f => f.endsWith('.json'))
      .map(f => f.replace('.json', ''));
    throw new Error(`Suite "${name}" not found. Available: ${available.join(', ') || 'none'}`);
  }

  return JSON.parse(fs.readFileSync(suitePath, 'utf-8'));
}

/**
 * List all available suites
 * @returns {Array} List of suite info
 */
export function listSuites() {
  const testsDir = path.join(__dirname, 'src', 'tests');
  if (!fs.existsSync(testsDir)) return [];

  return fs.readdirSync(testsDir)
    .filter(f => f.endsWith('.json'))
    .map(f => {
      const suite = JSON.parse(fs.readFileSync(path.join(testsDir, f), 'utf-8'));
      return {
        name: f.replace('.json', ''),
        title: suite.name,
        description: suite.description,
        setup: suite.setup,
        workflows: suite.workflows,
      };
    });
}

/**
 * Run a suite (setup workflow + child workflows)
 * @param {Object} page - Playwright page
 * @param {Object} request - Playwright API request context
 * @param {Object} suite - Suite definition
 * @param {Object} initialContext - Initial context values
 * @returns {Object} Final context after all workflows
 */
export async function runSuite(page, request, suite, initialContext = {}) {
  const totalWorkflows = (suite.setup ? 1 : 0) + (suite.workflows?.length || 0);
  let workflowNum = 0;

  console.log(`\n${'▓'.repeat(50)}`);
  console.log(`SUITE: ${suite.name}`);
  console.log(`${suite.description || ''}`);
  console.log(`Workflows: ${totalWorkflows}`);
  console.log(`${'▓'.repeat(50)}`);

  const suiteStartTime = Date.now();
  const workflowTimings: { name: string; duration: number }[] = [];

  // Run setup workflow first
  let context = { ...initialContext };

  if (suite.setup) {
    workflowNum++;
    console.log(`\n┌─ Workflow ${workflowNum}/${totalWorkflows}: SETUP (${suite.setup})`);
    const setupWorkflow = loadWorkflow(suite.setup);
    context = await runWorkflow(page, request, setupWorkflow, context);
    workflowTimings.push({ name: `SETUP: ${suite.setup}`, duration: context._workflowDuration || 0 });
    console.log(`└─ Setup complete`);
  }

  // Run each child workflow with the accumulated context
  const results = [];
  for (const workflowName of suite.workflows || []) {
    workflowNum++;
    console.log(`\n┌─ Workflow ${workflowNum}/${totalWorkflows}: ${workflowName}`);
    const workflow = loadWorkflow(workflowName);

    // Pass context from setup (and previous workflows) to this workflow
    const workflowContext = await runWorkflow(page, request, workflow, context);
    workflowTimings.push({ name: workflowName, duration: workflowContext._workflowDuration || 0 });

    // Accumulate context for subsequent workflows
    context = { ...context, ...workflowContext };
    results.push({ workflow: workflowName, context: workflowContext });
    console.log(`└─ Workflow complete`);
  }

  const suiteDuration = ((Date.now() - suiteStartTime) / 1000).toFixed(1);

  console.log(`\n${'▓'.repeat(50)}`);
  console.log(`✓ SUITE COMPLETE: ${suite.name} (${suiteDuration}s)`);
  console.log(`${'─'.repeat(50)}`);
  console.log(`TIMING SUMMARY:`);
  for (const { name, duration } of workflowTimings) {
    console.log(`  ${name}: ${duration.toFixed(1)}s`);
  }
  console.log(`  ${'─'.repeat(40)}`);
  console.log(`  TOTAL: ${suiteDuration}s`);
  console.log(`${'▓'.repeat(50)}\n`);

  return { context, results, _suiteDuration: parseFloat(suiteDuration), _workflowTimings: workflowTimings };
}

// CLI support - run workflows from command line
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const args = process.argv.slice(2);

  if (args.includes('--list') || args.length === 0) {
    console.log('\n=== Available Workflows ===\n');
    for (const w of listWorkflows()) {
      console.log(`  ${w.name}`);
      console.log(`    ${w.description}`);
      console.log(`    Steps: ${w.steps}${w.requires.length ? `, Requires: ${w.requires.join(', ')}` : ''}`);
      console.log();
    }

    console.log('\n=== Available Suites ===\n');
    for (const s of listSuites()) {
      console.log(`  ${s.name}`);
      console.log(`    ${s.description || ''}`);
      console.log(`    Setup: ${s.setup || 'none'}`);
      console.log(`    Workflows: ${s.workflows?.join(', ') || 'none'}`);
      console.log();
    }
  } else {
    console.log('To run a workflow: WORKFLOW=name npx playwright test workflow.spec.js');
    console.log('To run a suite:    SUITE=name npx playwright test suite.spec.js');
  }
}
