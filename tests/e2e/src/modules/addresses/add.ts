/**
 * Module: addresses.add
 *
 * Adds a new address to the workspace.
 */
import * as b from '../../blocks/index.js';
import { click, fill } from '../../utils';

export default {
  name: 'addresses.add',
  description: 'Add a new address to the workspace',

  requires: ['page', 'workspaceId'],
  produces: ['addressId', 'addressName'],

  async run(context) {
    const { page, workspaceId, config = {} } = context;

    const addressName = config.name || `Test Address ${Date.now()}`;
    const addressLine1 = config.addressLine1 || 'Test Street 123';
    const city = config.city || 'Budapest';
    const postalCode = config.postalCode || '1000';
    const country = config.country || 'Hungary';

    // Navigate to addresses list
    await b.goToAddresses(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Click add button
    await click(page, 'add-address-button');

    // Wait for modal to appear
    await b.waitForModal(page);

    // Fill required form fields
    await fill(page, 'address-name-input', addressName);
    await fill(page, 'address-address-line1-input', addressLine1);
    await fill(page, 'address-city-input', city);
    await fill(page, 'address-postal-code-input', postalCode);

    // Optional fields
    if (config.addressLine2) {
      await fill(page, 'address-address-line2-input', config.addressLine2);
    }
    if (country) {
      await fill(page, 'address-country-input', country);
    }
    if (config.contactPerson) {
      await fill(page, 'address-contact-person-input', config.contactPerson);
    }
    if (config.contactPhone) {
      await fill(page, 'address-contact-phone-input', config.contactPhone);
    }
    if (config.contactEmail) {
      await fill(page, 'address-contact-email-input', config.contactEmail);
    }

    // Click save
    await click(page, 'address-submit-button');

    // Wait for save and modal to close
    await b.waitForSave(page);
    await b.waitForModalClosed(page);

    // Give time for table to refresh after modal closes
    await page.waitForTimeout(1000);
    await b.waitForTableLoaded(page);

    // Navigate back to list and verify
    await b.goToAddresses(page, workspaceId);
    await b.waitForTableLoaded(page);

    // Search for the address name (new items may not be on first page due to sorting)
    const searchInput = page.locator('[data-testid="addresses-search-input"], input[placeholder*="keresés"], input[placeholder*="search"]').first();
    if (await searchInput.count() > 0) {
      await searchInput.fill(addressName);
      await searchInput.press('Enter');
      // Wait for search to execute and table to load results
      await page.waitForTimeout(2000);
      await b.waitForTableLoaded(page);
      await page.waitForTimeout(500);
    }

    await b.assertVisible(page, addressName, 15000);

    // Try to extract ID from the table row (optional)
    const row = await b.findRow(page, addressName).catch(() => null);
    let addressId = null;
    if (row) {
      const editButton = row.locator('[data-testid^="edit-address-"]').first();
      if (await editButton.count() > 0) {
        const testId = await editButton.getAttribute('data-testid');
        addressId = testId?.replace('edit-address-', '') || null;
      }
    }

    return {
      addressId,
      addressName,
    };
  },
};
