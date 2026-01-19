/**
 * Module: material-templates.delete
 *
 * Deletes a material template from the workspace.
 */
import * as b from '../../blocks/index.js';

export default {
  name: 'material-templates.delete',
  description: 'Delete a material template from the workspace',

  requires: ['page', 'workspaceId'],
  produces: ['materialTemplateDeleted'],

  async run(context) {
    const { page, workspaceId, materialTemplateId, materialTemplateName, config = {} } = context;

    const targetName = config.name || materialTemplateName;

    // Navigate to material templates list
    await b.goToMaterialTemplates(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Search for the template if we have a name (item may not be on first page due to sorting)
    if (targetName) {
      const searchInput = page.locator('[data-testid="material-templates-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
      if (await searchInput.count() > 0) {
        await searchInput.fill(targetName);
        await searchInput.press('Enter');
        await page.waitForTimeout(1500);
        await b.waitForTableLoaded(page);
      }
    }

    // Set up dialog handler before clicking delete (use once to avoid multiple handlers)
    page.once('dialog', async (dialog) => {
      console.log(`Dialog appeared: ${dialog.type()} - ${dialog.message()}`);
      await dialog.accept();
    });

    // Click delete button
    if (materialTemplateId) {
      await b.clickRowDelete(page, 'material-template', materialTemplateId);
    } else if (targetName) {
      const row = await b.findRow(page, targetName);
      const deleteBtn = row.locator('[data-testid^="delete-material-template-"]').first();
      await deleteBtn.click();
    } else {
      // Click first delete button
      await b.clickRowDelete(page, 'material-template');
    }

    // Wait for delete to complete and table to refresh
    await b.waitForSave(page);
    await b.waitForTableLoaded(page);

    // Verify the item is gone (should disappear from current search results)
    if (targetName) {
      await b.assertGone(page, targetName);
    }

    return {
      materialTemplateDeleted: true,
    };
  },
};
