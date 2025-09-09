table "user_global_roles" {
  schema = schema.public

  column "id" {
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
    expr = "role IN ('customizer', 'flex', 'labs')"
  }

  index "user_global_roles_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "user_global_roles_user_id_role_idx" {
    unique  = true
    columns = [column.user_id, column.role]
  }

  index "user_global_roles_role_idx" {
    columns = [column.role]
  }

  foreign_key "user_global_roles_user_id_fkey" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
} 