/**
 * Module: worksheets.edit
 *
 * Edits an existing worksheet in the workspace.
 */
import * as b from '../../blocks/index.js';
import { click, select } from '../../utils';

export default {
  name: 'worksheets.edit',
  description: 'Edit an existing worksheet',

  requires: ['page', 'workspaceId'],
  produces: ['worksheetUpdated'],

  async run(context) {
    const { page, workspaceId, worksheetId, config = {} } = context;

    // Navigate to worksheets list
    await b.goToWorksheets(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Click edit button
    if (worksheetId) {
      await click(page, `edit-worksheet-${worksheetId}`);
    } else {
      // Click first edit button
      const firstEditBtn = page.locator('[data-testid^="edit-worksheet-"]').first();
      await firstEditBtn.click();
    }

    // Wait for modal
    await b.waitForModal(page);

    // Update fields if provided
    if (config.workType) {
      await select(page, 'worksheet-work-type-input', config.workType);
    }
    if (config.serviceType) {
      await select(page, 'worksheet-service-type-input', config.serviceType);
    }
    if (config.status) {
      await select(page, 'worksheet-status-input', config.status);
    }

    // Update work description if provided
    if (config.workDescription) {
      const descTextarea = page.locator('[data-testid="worksheet-work-description-input"]');
      if (await descTextarea.count() > 0) {
        await descTextarea.fill(config.workDescription);
      }
    }

    // Update notes if provided
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

    return {
      worksheetUpdated: true,
    };
  },
};
