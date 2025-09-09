table "workstation_processes" {
  schema = schema.public

  column "workstation_id" {
    type = uuid
  }

  column "process_id" {
    type = uuid
  }

  column "created_at" {
    type = timestamp
    default = sql("CURRENT_TIMESTAMP")
  }

  primary_key {
    columns = [column.workstation_id, column.process_id]
  }

  index "workstation_processes_workstation_id_idx" {
    columns = [column.workstation_id]
  }

  index "workstation_processes_process_id_idx" {
    columns = [column.process_id]
  }

  index "workstation_processes_unique_idx" {
    unique = true
    columns = [column.workstation_id, column.process_id]
  }

  foreign_key "workstation_processes_workstation_id_fkey" {
    columns = [column.workstation_id]
    ref_columns = [table.workstations.column.id]
    on_delete = CASCADE
  }

  foreign_key "workstation_processes_process_id_fkey" {
    columns = [column.process_id]
    ref_columns = [table.processes.column.id]
    on_delete = CASCADE
  }
} 