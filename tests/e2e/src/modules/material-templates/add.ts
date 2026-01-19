/**
 * Module: material-templates.add
 *
 * Adds a new material template to the workspace.
 */
import * as b from '../../blocks/index.js';
import { click, fill, waitFor } from '../../utils';

export default {
  name: 'material-templates.add',
  description: 'Add a new material template to the workspace',

  requires: ['page', 'workspaceId'],
  produces: ['materialTemplateId', 'materialTemplateName'],

  async run(context) {
    const { page, workspaceId, config = {} } = context;

    const templateName = config.name || `Test Material ${Date.now()}`;
    const unit = config.unit || 'pcs';
    const category = config.category || 'E2E Test';
    const description = config.description || 'Created during e2e testing';

    // Navigate to material templates list
    await b.goToMaterialTemplates(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Click add button
    await click(page, 'add-material-template-button');

    // Wait for modal to appear
    await b.waitForModal(page);

    // Fill form fields
    await fill(page, 'material-template-name-input', templateName);
    await fill(page, 'material-template-unit-input', unit);

    // Optional fields
    if (category) {
      await fill(page, 'material-template-category-input', category);
    }
    if (description) {
      const descInput = page.locator('[data-testid="material-template-description-input"]');
      if (await descInput.count() > 0) {
        await descInput.fill(description);
      }
    }

    // Click save
    await click(page, 'material-template-submit-button');

    // Wait for save and modal to close
    await b.waitForSave(page);
    await b.waitForModalClosed(page);

    // Give time for the table to reload after modal closes
    await page.waitForTimeout(1000);
    await b.waitForTableLoaded(page);

    // Search for the template name (new items may not be on first page due to sorting)
    const searchInput = page.locator('[data-testid="material-templates-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
    if (await searchInput.count() > 0) {
      await searchInput.fill(templateName);
      await searchInput.press('Enter');
      await page.waitForTimeout(1500);
      await b.waitForTableLoaded(page);
    }

    // Verify new item appears in table
    await b.assertVisible(page, templateName, 15000);

    // Try to extract ID from the table row (optional)
    const row = await b.findRow(page, templateName).catch(() => null);
    let materialTemplateId = null;
    if (row) {
      const editButton = row.locator('[data-testid^="edit-material-template-"]').first();
      if (await editButton.count() > 0) {
        const testId = await editButton.getAttribute('data-testid');
        materialTemplateId = testId?.replace('edit-material-template-', '') || null;
      }
    }

    return {
      materialTemplateId,
      materialTemplateName: templateName,
    };
  },
};
