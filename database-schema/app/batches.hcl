table "batches" {
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

  column "job_id" {
    null = false
    type = uuid
  }

  column "part_id" {
    null = true
    type = uuid
  }

  column "look_id" {
    null = true
    type = uuid
  }

  column "form_data" {
    null = true
    type = jsonb
  }

  column "description" {
    null = true
    type = text
  }

  column "quantity" {
    null = false
    type = integer
  }

  column "status" {
    null = true
    type = varchar(255)
  }

  column "current_step" {
    null = false
    type = integer
    default = 1
  }

  column "current_workstation_id" {
    null = true
    type = uuid
  }

  column "previous_workstation_id" {
    null = true
    type = uuid
  }

  column "workflow_state" {
    null = false
    type = varchar(50)
    default = "to-do"
  }

  column "is_current" {
    null = false
    type = boolean
    default = false
  }

  column "created_at" {
    null = false
    type = timestamp
    default = sql("NOW()")
  }

  column "updated_at" {
    null = false
    type = timestamp
    default = sql("NOW()")
  }

  primary_key {
    columns = [column.id]
  }

  foreign_key "fk_batches_workspace" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "fk_batches_order" {
    columns     = [column.order_id]
    ref_columns = [table.orders.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "fk_batches_job" {
    columns     = [column.job_id]
    ref_columns = [table.jobs.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "fk_batches_part" {
    columns     = [column.part_id]
    ref_columns = [table.parts.column.id]
    on_update   = NO_ACTION
    on_delete   = SET_NULL
  }

  foreign_key "fk_batches_look" {
    columns     = [column.look_id]
    ref_columns = [table.looks.column.id]
    on_update   = NO_ACTION
    on_delete   = SET_NULL
  }

  foreign_key "fk_batches_current_workstation" {
    columns     = [column.current_workstation_id]
    ref_columns = [table.workstations.column.id]
    on_update   = NO_ACTION
    on_delete   = SET_NULL
  }

  foreign_key "fk_batches_previous_workstation" {
    columns     = [column.previous_workstation_id]
    ref_columns = [table.workstations.column.id]
    on_update   = NO_ACTION
    on_delete   = SET_NULL
  }

  check "quantity_positive" {
    expr = "quantity > 0"
  }

  check "status_valid" {
    expr = "status IN ('awaiting', 'in-progress', 'complete')"
  }

  check "current_step_positive" {
    expr = "current_step > 0"
  }

  check "workflow_state_valid" {
    expr = "workflow_state IN ('to-do', 'doing', 'done', 'paused')"
  }

  index "batches_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "batches_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "batches_order_id_idx" {
    columns = [column.order_id]
  }

  index "batches_job_id_idx" {
    columns = [column.job_id]
  }

  index "batches_part_id_idx" {
    columns = [column.part_id]
  }

  index "batches_look_id_idx" {
    columns = [column.look_id]
  }

  index "batches_status_idx" {
    columns = [column.status]
  }

  index "batches_current_step_idx" {
    columns = [column.current_step]
  }

  index "batches_workflow_state_idx" {
    columns = [column.workflow_state]
  }

  index "batches_current_workstation_idx" {
    columns = [column.current_workstation_id]
  }

  index "batches_previous_workstation_idx" {
    columns = [column.previous_workstation_id]
  }
} 