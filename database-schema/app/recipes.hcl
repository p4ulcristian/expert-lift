table "recipes" {
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
    type = timestamp
    default = sql("CURRENT_TIMESTAMP")
  }

  column "updated_at" {
    null = false
    type = timestamp
    default = sql("CURRENT_TIMESTAMP")
  }

  index "recipes_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "recipes_name_idx" {
    unique  = true
    columns = [column.name]
  }

  foreign_key "recipes_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
} 