table "processes" {
  schema = schema.public

  column "id" {
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

  column "workspace_id" {
    null = false
    type = uuid
  }

  column "created_at" {
    null = false
    type = timestamptz
    default = "now()"
  }

  column "updated_at" {
    null = false
    type = timestamptz
    default = "now()"
  }

  index "processes_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "processes_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  foreign_key "processes_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
} 