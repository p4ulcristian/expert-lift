table "workspace_shares" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }

  column "workspace_id" {
    null = false
    type = uuid
  }

  column "user_id" {
    null = false
    type = uuid
  }

  column "role" {
    null = false
    type = varchar(20)
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

  check "role_valid" {
    expr = "role IN ('owner', 'employee')"
  }

  index "workspace_shares_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "workspace_shares_unique_idx" {
    unique  = true
    columns = [column.workspace_id, column.user_id]
  }

  index "workspace_shares_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "workspace_shares_user_id_idx" {
    columns = [column.user_id]
  }

  index "workspace_shares_role_idx" {
    columns = [column.role]
  }

  foreign_key "workspace_shares_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "workspace_shares_user_id_fkey" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
} 