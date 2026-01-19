export interface AddressData {
  name: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  postalCode: string;
  country?: string;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  elevators?: string[];
}

export const TEST_ADDRESSES: Record<string, AddressData> = {
  BUDAPEST_OFFICE: {
    name: `Budapest Office ${Date.now()}`,
    addressLine1: 'Andrássy út 123',
    city: 'Budapest',
    postalCode: '1061',
    country: 'Hungary',
    contactPerson: 'Test Contact',
    contactPhone: '+36 1 234 5678',
    contactEmail: 'test@example.com',
    elevators: ['E1', 'E2'],
  },
  RESIDENTIAL: {
    name: `Residential Building ${Date.now()}`,
    addressLine1: 'Váci utca 45',
    addressLine2: '3rd floor',
    city: 'Budapest',
    postalCode: '1052',
    country: 'Hungary',
    contactPerson: 'Building Manager',
    contactPhone: '+36 20 987 6543',
    elevators: ['MAIN'],
  },
};

export function generateAddress(prefix = 'Test'): AddressData {
  return {
    name: `${prefix} Address ${Date.now()}`,
    addressLine1: 'Test Street 123',
    city: 'Budapest',
    postalCode: '1000',
    country: 'Hungary',
    contactPerson: 'E2E Test Contact',
    contactPhone: '+36 1 000 0000',
    contactEmail: 'e2e-test@example.com',
    elevators: [],
  };
}
