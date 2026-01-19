/**
 * Module: addresses.delete
 *
 * Deletes an address from the workspace.
 */
import * as b from '../../blocks/index.js';

export default {
  name: 'addresses.delete',
  description: 'Delete an address from the workspace',

  requires: ['page', 'workspaceId'],
  produces: ['addressDeleted'],

  async run(context) {
    const { page, workspaceId, addressId, addressName, config = {} } = context;

    const targetName = config.name || addressName;

    // Navigate to addresses list
    await b.goToAddresses(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Search for the address if we have a name (item may not be on first page due to sorting)
    if (targetName) {
      const searchInput = page.locator('[data-testid="addresses-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
      if (await searchInput.count() > 0) {
        await searchInput.fill(targetName);
        await searchInput.press('Enter');
        await page.waitForTimeout(2000);
        await b.waitForTableLoaded(page);
        await page.waitForTimeout(500);
      }
    }

    // Set up dialog handler before clicking delete (use once to avoid multiple handlers)
    page.once('dialog', async (dialog) => {
      await dialog.accept();
    });

    // Click delete button
    if (addressId) {
      await b.clickRowDelete(page, 'address', addressId);
    } else if (targetName) {
      const row = await b.findRow(page, targetName);
      const deleteBtn = row.locator('[data-testid^="delete-address-"]').first();
      await deleteBtn.click();
    } else {
      // Click first delete button
      await b.clickRowDelete(page, 'address');
    }

    // Wait for delete to complete and table to refresh
    await b.waitForSave(page);
    await b.waitForTableLoaded(page);

    // Verify the item is gone (should disappear from current search results)
    if (targetName) {
      await b.assertGone(page, targetName);
    }

    return {
      addressDeleted: true,
    };
  },
};
