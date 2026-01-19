/**
 * Module: material-templates.edit
 *
 * Edits an existing material template in the workspace.
 */
import * as b from '../../blocks/index.js';
import { click, fill } from '../../utils';

export default {
  name: 'material-templates.edit',
  description: 'Edit an existing material template',

  requires: ['page', 'workspaceId'],
  produces: ['materialTemplateUpdated', 'materialTemplateName'],

  async run(context) {
    const { page, workspaceId, materialTemplateId, materialTemplateName, config = {} } = context;

    const updatedName = config.name || `Updated Material ${Date.now()}`;

    // Navigate to material templates list
    await b.goToMaterialTemplates(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Search for the template if we have a name (item may not be on first page due to sorting)
    if (materialTemplateName) {
      const searchInput = page.locator('[data-testid="material-templates-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
      if (await searchInput.count() > 0) {
        await searchInput.fill(materialTemplateName);
        await searchInput.press('Enter');
        await page.waitForTimeout(1500);
        await b.waitForTableLoaded(page);
      }
    }

    // Click edit button
    if (materialTemplateId) {
      await b.clickRowEdit(page, 'material-template', materialTemplateId);
    } else if (materialTemplateName) {
      const row = await b.findRow(page, materialTemplateName);
      const editBtn = row.locator('[data-testid^="edit-material-template-"]').first();
      await editBtn.click();
    } else {
      // Click first edit button
      await b.clickRowEdit(page, 'material-template');
    }

    // Wait for modal
    await b.waitForModal(page);

    // Update name
    await fill(page, 'material-template-name-input', updatedName);

    // Update other fields if provided
    if (config.unit) {
      await fill(page, 'material-template-unit-input', config.unit);
    }
    if (config.category) {
      await fill(page, 'material-template-category-input', config.category);
    }

    // Click save
    await click(page, 'material-template-submit-button');

    // Wait for save and modal to close
    await b.waitForSave(page);
    await b.waitForModalClosed(page);

    // Navigate back to list and verify
    await b.goToMaterialTemplates(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Search for the updated name (may not be on first page due to sorting)
    const searchInput = page.locator('[data-testid="material-templates-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
    if (await searchInput.count() > 0) {
      await searchInput.fill(updatedName);
      await searchInput.press('Enter');
      await page.waitForTimeout(1500);
      await b.waitForTableLoaded(page);
    }

    await b.assertVisible(page, updatedName);

    return {
      materialTemplateUpdated: true,
      materialTemplateName: updatedName,
    };
  },
};
