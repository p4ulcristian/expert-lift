export interface MaterialTemplateData {
  name: string;
  unit: string;
  category?: string;
  description?: string;
}

export const TEST_MATERIAL_TEMPLATES: Record<string, MaterialTemplateData> = {
  BASIC: {
    name: `Test Material ${Date.now()}`,
    unit: 'pcs',
    category: 'Test Category',
    description: 'Test material description for e2e testing',
  },
  CABLE: {
    name: `Cable ${Date.now()}`,
    unit: 'm',
    category: 'Electrical',
    description: 'Electrical cable material',
  },
  OIL: {
    name: `Lubricant ${Date.now()}`,
    unit: 'L',
    category: 'Maintenance',
    description: 'Machine lubricant',
  },
};

export function generateMaterialTemplate(prefix = 'Test'): MaterialTemplateData {
  return {
    name: `${prefix} Material ${Date.now()}`,
    unit: 'pcs',
    category: 'E2E Test',
    description: `Material created during e2e test at ${new Date().toISOString()}`,
  };
}
