table "workstations" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "workspace_id" {
    null = false
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

  index "workstations_id_idx" {
    unique  = true
    columns = [column.id]
  }

  foreign_key "workstations_workspace_id_fkey" {
    columns = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_delete = CASCADE
  }
} 