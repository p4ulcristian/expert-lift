/**
 * Module Registry
 *
 * Central registry for all test modules. Each module is a reusable test step
 * that can be composed into workflows.
 */

// Workspace modules
import workspaceLogin from './workspace/login.ts';

// Material Templates modules
import materialTemplatesAdd from './material-templates/add.ts';
import materialTemplatesEdit from './material-templates/edit.ts';
import materialTemplatesDelete from './material-templates/delete.ts';

// Addresses modules
import addressesAdd from './addresses/add.ts';
import addressesEdit from './addresses/edit.ts';
import addressesDelete from './addresses/delete.ts';

// Worksheets modules
import worksheetsAdd from './worksheets/add.ts';
import worksheetsEdit from './worksheets/edit.ts';
import worksheetsDelete from './worksheets/delete.ts';

// All registered modules
export const modules = {
  // Workspace
  'workspace.login': workspaceLogin,

  // Material Templates
  'material-templates.add': materialTemplatesAdd,
  'material-templates.edit': materialTemplatesEdit,
  'material-templates.delete': materialTemplatesDelete,

  // Addresses
  'addresses.add': addressesAdd,
  'addresses.edit': addressesEdit,
  'addresses.delete': addressesDelete,

  // Worksheets
  'worksheets.add': worksheetsAdd,
  'worksheets.edit': worksheetsEdit,
  'worksheets.delete': worksheetsDelete,
};

/**
 * Get a module by name
 * @param {string} name - Module name (e.g., 'workspace.login')
 * @returns {Object} Module definition
 */
export function getModule(name) {
  const mod = modules[name];
  if (!mod) {
    const available = Object.keys(modules).join(', ');
    throw new Error(`Unknown module: "${name}". Available: ${available}`);
  }
  return mod;
}

/**
 * List all available modules
 * @returns {Array} List of module info
 */
export function listModules() {
  return Object.entries(modules).map(([name, mod]) => ({
    name,
    description: mod.description,
    requires: mod.requires || [],
    produces: mod.produces || [],
  }));
}

/**
 * Validate a workflow's module dependencies
 * @param {Object} workflow - Workflow definition
 * @param {Object} initialContext - Optional initial context (e.g., from suite setup)
 * @returns {Object} Validation result { valid: boolean, errors: string[] }
 */
export function validateWorkflow(workflow, initialContext = {}) {
  const errors = [];
  const available = new Set(['page', 'request', 'config', ...Object.keys(initialContext)]); // Always available + initial context

  for (const step of workflow.steps) {
    const mod = modules[step.module];

    if (!mod) {
      errors.push(`Unknown module: ${step.module}`);
      continue;
    }

    // Check required context
    for (const req of mod.requires || []) {
      if (!available.has(req)) {
        errors.push(`Module "${step.module}" requires "${req}" but it's not available yet`);
      }
    }

    // Add produced values to available context
    for (const prod of mod.produces || []) {
      available.add(prod);
    }
  }

  return {
    valid: errors.length === 0,
    errors,
  };
}
