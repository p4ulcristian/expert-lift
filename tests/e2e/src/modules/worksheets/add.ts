/**
 * Module: worksheets.add
 *
 * Adds a new worksheet to the workspace.
 */
import * as b from '../../blocks/index.js';
import { click, select } from '../../utils';

export default {
  name: 'worksheets.add',
  description: 'Add a new worksheet to the workspace',

  requires: ['page', 'workspaceId'],
  produces: ['worksheetId', 'worksheetSerialNumber'],

  async run(context) {
    const { page, workspaceId, config = {} } = context;

    const workType = config.workType || 'maintenance';
    const serviceType = config.serviceType || 'normal';
    const workDescription = config.workDescription || `E2E Test Worksheet ${Date.now()}`;
    const status = config.status || 'draft';

    // Navigate to worksheets list
    await b.goToWorksheets(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Click add button
    await click(page, 'add-worksheet-button');

    // Wait for modal to appear
    await b.waitForModal(page);

    // Fill form fields using select dropdowns
    await select(page, 'worksheet-work-type-input', workType);
    await select(page, 'worksheet-service-type-input', serviceType);
    await select(page, 'worksheet-status-input', status);

    // Fill work description textarea
    const descTextarea = page.locator('[data-testid="worksheet-work-description-input"]');
    if (await descTextarea.count() > 0) {
      await descTextarea.fill(workDescription);
    }

    // Optional: Fill address if provided (address autocomplete)
    if (config.addressName) {
      // Address field uses autocomplete - type to search
      const addressInput = page.locator('[data-testid="worksheet-address-input"], .address-search-input').first();
      if (await addressInput.count() > 0) {
        await addressInput.fill(config.addressName);
        await page.waitForTimeout(500);
        // Click first suggestion
        const suggestion = page.locator('.address-suggestion, [data-testid="address-option"]').first();
        if (await suggestion.count() > 0) {
          await suggestion.click();
        }
      }
    }

    // Optional: Fill notes
    if (config.notes) {
      const notesTextarea = page.locator('[data-testid="worksheet-notes-input"]');
      if (await notesTextarea.count() > 0) {
        await notesTextarea.fill(config.notes);
      }
    }

    // Click save
    await click(page, 'worksheet-submit-button');

    // Wait for save and modal to close
    await b.waitForSave(page);
    await b.waitForModalClosed(page);

    // Navigate back to list
    await b.goToWorksheets(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Try to find the worksheet and extract ID
    // Worksheets may have serial numbers - look for the newest one
    let worksheetId = null;
    let worksheetSerialNumber = null;

    // Look for the first edit button to get the ID
    const firstEditBtn = page.locator('[data-testid^="edit-worksheet-"]').first();
    if (await firstEditBtn.count() > 0) {
      const testId = await firstEditBtn.getAttribute('data-testid');
      worksheetId = testId?.replace('edit-worksheet-', '') || null;
    }

    return {
      worksheetId,
      worksheetSerialNumber,
    };
  },
};
