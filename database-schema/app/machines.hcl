table "machines" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "workspace_id" {
    null = false
    type = uuid
  }

  column "workstation_id" {
    null = true
    type = uuid
  }

  column "name" {
    null = false
    type = text
  }

  column "description" {
    null = true
    type = text
  }

  column "category" {
    null = true
    type = text
    default = "Custom"
  }

  column "status" {
    null = true
    type = text
    default = "Idle"
  }

  column "location" {
    null = true
    type = text
  }

  // Energy Consumption Profiles (JSONB Array)
  column "energy_profiles" {
    null = true
    type = jsonb
    default = "[]"
  }

  // Amortization Fields
  column "amortization_time_based" {
    null = true
    type = boolean
    default = false
  }

  column "amortization_usage_based" {
    null = true
    type = boolean
    default = false
  }

  column "amortization_time_rate" {
    null = true
    type = decimal(10, 2)
    default = 0
  }

  column "amortization_usage_rate" {
    null = true
    type = decimal(10, 2)
    default = 0
  }

  column "usage_unit" {
    null = true
    type = text
  }


  // Maintenance Fields
  column "last_maintenance" {
    null = true
    type = date
  }

  column "maintenance_interval_days" {
    null = true
    type = integer
    default = 30
  }

  column "maintenance_due" {
    null = true
    type = date
  }

  column "wear_parts" {
    null = true
    type = jsonb
    default = "[]"
  }

  // Usage Tracking
  column "last_used" {
    null = true
    type = timestamptz
  }

  column "created_at" {
    null = false
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  column "updated_at" {
    null = false
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  index "machines_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "machines_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "machines_status_idx" {
    columns = [column.status]
  }

  index "machines_category_idx" {
    columns = [column.category]
  }


  index "machines_maintenance_due_idx" {
    columns = [column.maintenance_due]
  }

  foreign_key "machines_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "machines_workstation_id_fkey" {
    columns     = [column.workstation_id]
    ref_columns = [table.workstations.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
} 