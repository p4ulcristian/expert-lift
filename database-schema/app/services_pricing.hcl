table "services_pricing" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "workspace_id" {
    null = false
    type = uuid
  }

  column "service_id" {
    null = false
    type = uuid
  }

  column "price" {
    null = false
    type = decimal(10,2)
    default = 0
  }

  column "is_active" {
    null = false
    type = boolean
    default = false
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

  primary_key {
    columns = [column.id]
  }

  foreign_key "services_pricing_service_id_fkey" {
    columns     = [column.service_id]
    ref_columns = [table.services.column.id]
    on_delete   = CASCADE
  }

  foreign_key "services_pricing_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  index "services_pricing_workspace_service_idx" {
    unique  = true
    columns = [column.workspace_id, column.service_id]
  }

  index "services_pricing_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "services_pricing_service_id_idx" {
    columns = [column.service_id]
  }
} 