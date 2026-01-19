/**
 * Module: worksheets.delete
 *
 * Deletes a worksheet from the workspace.
 */
import * as b from '../../blocks/index.js';
import { click } from '../../utils';

export default {
  name: 'worksheets.delete',
  description: 'Delete a worksheet from the workspace',

  requires: ['page', 'workspaceId'],
  produces: ['worksheetDeleted'],

  async run(context) {
    const { page, workspaceId, worksheetId, config = {} } = context;

    // Navigate to worksheets list
    await b.goToWorksheets(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Set up dialog handler before clicking delete
    b.handleConfirmDialog(page);

    // Click delete button
    if (worksheetId || config.worksheetId) {
      const targetId = config.worksheetId || worksheetId;
      await click(page, `delete-worksheet-${targetId}`);
    } else {
      // Click first delete button
      const firstDeleteBtn = page.locator('[data-testid^="delete-worksheet-"]').first();
      await firstDeleteBtn.click();
    }

    // Wait for save/deletion
    await b.waitForSave(page);

    // Navigate back to list
    await b.goToWorksheets(page, workspaceId);
    await b.waitForTableLoaded(page);

    return {
      worksheetDeleted: true,
    };
  },
};
