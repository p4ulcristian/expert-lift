#!/usr/bin/env node
/**
 * CLI Module Runner
 *
 * Run individual modules for debugging:
 *   bun run-module.ts material-templates.add --headed
 *   bun run-module.ts addresses.edit --config='{"name":"Updated Name"}'
 *   bun run-module.ts worksheets.delete --context='{"workspaceId":"ws_abc123"}'
 *   bun run-module.ts --list
 */

import { chromium } from 'playwright';
import { expect } from '@playwright/test';
import { getModule, listModules } from './src/modules/registry.ts';

const args = process.argv.slice(2);

// Show help
if (args.length === 0 || args.includes('--help') || args.includes('-h')) {
  console.log(`
CLI Module Runner

Usage:
  node run-module.js <module-name> [options]

Options:
  --headed              Run browser in headed mode (visible)
  --config='{"k":"v"}'  Pass config object to module
  --context='{"k":"v"}' Pass initial context (for modules with requirements)
  --list                List all available modules
  --help, -h            Show this help

Examples:
  bun run-module.ts material-templates.add --headed
  bun run-module.ts addresses.edit --config='{"name":"Updated Name"}'
  bun run-module.ts worksheets.delete --context='{"workspaceId":"ws_abc123"}'
  bun run-module.ts --list
`);
  process.exit(0);
}

// List modules
if (args.includes('--list')) {
  console.log('\nAvailable Modules:\n');
  const mods = listModules();

  // Group by namespace (extract from namespace.name format)
  const categories = {};
  for (const mod of mods) {
    const [category] = mod.name.split('.');
    if (!categories[category]) categories[category] = [];
    categories[category].push(mod);
  }

  for (const [category, categoryMods] of Object.entries(categories)) {
    console.log(`  ${category.toUpperCase()}`);
    for (const mod of categoryMods) {
      console.log(`    ${mod.name}`);
      console.log(`      ${mod.description}`);
      if (mod.requires.length > 0) {
        console.log(`      requires: ${mod.requires.join(', ')}`);
      }
      if (mod.produces.length > 0) {
        console.log(`      produces: ${mod.produces.join(', ')}`);
      }
    }
    console.log();
  }
  process.exit(0);
}

// Parse arguments
const moduleName = args.find(a => !a.startsWith('--'));
const headed = args.includes('--headed');

// Parse --config and --context
function parseJsonArg(prefix) {
  const arg = args.find(a => a.startsWith(prefix));
  if (!arg) return {};
  const json = arg.slice(prefix.length);
  try {
    return JSON.parse(json);
  } catch (e) {
    console.error(`Error parsing ${prefix}: ${e.message}`);
    console.error(`  Got: ${json}`);
    process.exit(1);
  }
}

const config = parseJsonArg('--config=');
const initialContext = parseJsonArg('--context=');

if (!moduleName) {
  console.error('Error: Module name required');
  console.error('Usage: node run-module.js <module-name> [options]');
  console.error('Run with --list to see available modules');
  process.exit(1);
}

// Get the module
let mod;
try {
  mod = getModule(moduleName);
} catch (e) {
  console.error(`Error: ${e.message}`);
  process.exit(1);
}

console.log(`\n${'='.repeat(60)}`);
console.log(`MODULE: ${mod.name}`);
console.log(`${mod.description}`);
console.log(`${'='.repeat(60)}\n`);

if (Object.keys(config).length > 0) {
  console.log('Config:', JSON.stringify(config, null, 2));
}
if (Object.keys(initialContext).length > 0) {
  console.log('Initial Context:', JSON.stringify(initialContext, null, 2));
}

// Check requirements
const missingRequirements = (mod.requires || []).filter(
  req => req !== 'page' && req !== 'config' && !(req in initialContext)
);

if (missingRequirements.length > 0) {
  console.log('\n⚠️  Warning: Module requires context not provided:');
  console.log(`   Missing: ${missingRequirements.join(', ')}`);
  console.log('   Pass with --context=\'{"key":"value"}\'');
  console.log('   Continuing anyway...\n');
}

// Run the module
async function run() {
  const browser = await chromium.launch({
    headless: !headed,
  });

  const context = await browser.newContext({
    baseURL: 'https://localhost',
    ignoreHTTPSErrors: true,
  });

  const page = await context.newPage();
  page.setDefaultTimeout(15000);

  // Set viewport to match Playwright's Desktop Chrome default
  await page.setViewportSize({ width: 1280, height: 720 });

  try {
    const startTime = Date.now();

    const result = await mod.run({
      page,
      expect,
      config,
      ...initialContext,
    });

    const duration = ((Date.now() - startTime) / 1000).toFixed(1);

    console.log(`\n${'='.repeat(60)}`);
    console.log(`MODULE COMPLETE: ${mod.name} (${duration}s)`);
    console.log(`${'='.repeat(60)}\n`);

    if (result) {
      console.log('Result:', JSON.stringify(result, null, 2));
    }

    // Keep browser open if headed (for debugging)
    if (headed) {
      console.log('\n📌 Browser kept open for debugging. Press Ctrl+C to close.\n');
      await new Promise(() => {}); // Keep running
    }

  } catch (error) {
    console.error(`\n❌ Module failed: ${error.message}`);
    console.error(error.stack);

    // Keep browser open on error if headed
    if (headed) {
      console.log('\n📌 Browser kept open for debugging. Press Ctrl+C to close.\n');
      await new Promise(() => {});
    }

    process.exit(1);
  } finally {
    if (!headed) {
      await browser.close();
    }
  }
}

run();
