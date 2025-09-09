table "jobs" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "workspace_id" {
    null = false
    type = uuid
  }

  column "order_id" {
    null = false
    type = uuid
  }

  column "package_id" {
    null = false
    type = uuid
  }

  column "description" {
    null = true
    type = text
  }

  column "form_data" {
    null = true
    type = jsonb
  }

  column "created_at" {
    null = false
    type = timestamp
    default = sql("now()")
  }

  column "status" {
    null = false
    type = varchar(255)
    default = "'pending'"
  }

  foreign_key "fk_jobs_workspace" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "fk_jobs_order" {
    columns     = [column.order_id]
    ref_columns = [table.orders.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "fk_jobs_package" {
    columns     = [column.package_id]
    ref_columns = [table.packages.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  check "status_valid" {
    expr = "status IN ('pending', 'awaiting-inspection', 'inspected', 'in-progress', 'paused', 'job-complete')"
  }

  primary_key {
    columns = [column.id]
  }
}