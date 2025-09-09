table "batch_processes" {
  schema = schema.public

  column "id" {
    null = false
    type = serial
  }

  column "batch_id" {
    null = false
    type = uuid
  }

  column "process_id" {
    null = false
    type = uuid
  }

  column "step_order" {
    null = false
    type = integer
  }

  column "start_time" {
    null = true
    type = timestamp
  }

  column "finish_time" {
    null = true
    type = timestamp
  }

  column "created_at" {
    null = false
    type = timestamp
    default = sql("CURRENT_TIMESTAMP")
  }

  primary_key {
    columns = [column.id]
  }

  foreign_key "batch_processes_batch_id_fkey" {
    columns     = [column.batch_id]
    ref_columns = [table.batches.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "batch_processes_process_id_fkey" {
    columns     = [column.process_id]
    ref_columns = [table.processes.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  index "batch_processes_batch_id_idx" {
    columns = [column.batch_id]
  }

  index "batch_processes_process_id_idx" {
    columns = [column.process_id]
  }

  index "batch_processes_step_order_idx" {
    columns = [column.step_order]
  }

  index "batch_processes_start_time_idx" {
    columns = [column.start_time]
  }

  index "batch_processes_finish_time_idx" {
    columns = [column.finish_time]
  }

  check "step_order_positive" {
    expr = "step_order > 0"
  }
} 