/**
 * Module: addresses.edit
 *
 * Edits an existing address in the workspace.
 */
import * as b from '../../blocks/index.js';
import { click, fill } from '../../utils';

export default {
  name: 'addresses.edit',
  description: 'Edit an existing address',

  requires: ['page', 'workspaceId'],
  produces: ['addressUpdated', 'addressName'],

  async run(context) {
    const { page, workspaceId, addressId, addressName, config = {} } = context;

    const updatedName = config.name || `Updated Address ${Date.now()}`;

    // Navigate to addresses list
    await b.goToAddresses(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Search for the address if we have a name (item may not be on first page due to sorting)
    if (addressName) {
      const searchInput = page.locator('[data-testid="addresses-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
      if (await searchInput.count() > 0) {
        await searchInput.fill(addressName);
        await searchInput.press('Enter');
        await page.waitForTimeout(1500);
        await b.waitForTableLoaded(page);
      }
    }

    // Click edit button
    if (addressId) {
      await b.clickRowEdit(page, 'address', addressId);
    } else if (addressName) {
      const row = await b.findRow(page, addressName);
      const editBtn = row.locator('[data-testid^="edit-address-"]').first();
      await editBtn.click();
    } else {
      // Click first edit button
      await b.clickRowEdit(page, 'address');
    }

    // Wait for modal
    await b.waitForModal(page);

    // Update name
    await fill(page, 'address-name-input', updatedName);

    // Update other fields if provided
    if (config.addressLine1) {
      await fill(page, 'address-address-line1-input', config.addressLine1);
    }
    if (config.city) {
      await fill(page, 'address-city-input', config.city);
    }
    if (config.postalCode) {
      await fill(page, 'address-postal-code-input', config.postalCode);
    }

    // Click save
    await click(page, 'address-submit-button');

    // Wait for save and modal to close
    await b.waitForSave(page);
    await b.waitForModalClosed(page);

    // Navigate back to list and verify
    await b.goToAddresses(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Search for the updated name (may not be on first page due to sorting)
    const searchInput = page.locator('[data-testid="addresses-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
    if (await searchInput.count() > 0) {
      await searchInput.fill(updatedName);
      await searchInput.press('Enter');
      await page.waitForTimeout(2000);
      await b.waitForTableLoaded(page);
      await page.waitForTimeout(500);
    }

    await b.assertVisible(page, updatedName);

    return {
      addressUpdated: true,
      addressName: updatedName,
    };
  },
};
