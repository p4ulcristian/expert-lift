export interface WorksheetData {
  workType: string;
  serviceType: string;
  workDescription: string;
  status: string;
  notes?: string;
}

export const WORK_TYPES = {
  INSTALLATION: 'installation',
  MAINTENANCE: 'maintenance',
  REPAIR: 'repair',
  INSPECTION: 'inspection',
  MODERNIZATION: 'modernization',
};

export const SERVICE_TYPES = {
  REGULAR: 'regular',
  EMERGENCY: 'emergency',
  SCHEDULED: 'scheduled',
  ON_CALL: 'on-call',
};

export const STATUSES = {
  DRAFT: 'draft',
  IN_PROGRESS: 'in_progress',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled',
};

export const TEST_WORKSHEETS: Record<string, WorksheetData> = {
  BASIC: {
    workType: WORK_TYPES.MAINTENANCE,
    serviceType: SERVICE_TYPES.REGULAR,
    workDescription: 'Regular maintenance check - e2e test',
    status: STATUSES.DRAFT,
    notes: 'Created during e2e testing',
  },
  REPAIR: {
    workType: WORK_TYPES.REPAIR,
    serviceType: SERVICE_TYPES.EMERGENCY,
    workDescription: 'Emergency repair - elevator stuck',
    status: STATUSES.IN_PROGRESS,
    notes: 'Urgent repair needed',
  },
  INSPECTION: {
    workType: WORK_TYPES.INSPECTION,
    serviceType: SERVICE_TYPES.SCHEDULED,
    workDescription: 'Annual safety inspection',
    status: STATUSES.DRAFT,
    notes: 'Scheduled annual inspection',
  },
};

export function generateWorksheet(prefix = 'Test'): WorksheetData {
  return {
    workType: WORK_TYPES.MAINTENANCE,
    serviceType: SERVICE_TYPES.REGULAR,
    workDescription: `${prefix} worksheet - created at ${new Date().toISOString()}`,
    status: STATUSES.DRAFT,
    notes: 'Created during e2e testing',
  };
}
