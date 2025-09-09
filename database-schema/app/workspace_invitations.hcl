table "workspace_invitations" {
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

  column "email" {
    null = false
    type = varchar(255)
  }

  column "role" {
    null = false
    type = varchar(20)
  }

  column "invited_by" {
    null = false
    type = uuid
  }

  column "status" {
    null = false
    type = varchar(20)
    default = "pending"
  }

  column "expires_at" {
    null = false
    type = timestamptz
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

  check "status_valid" {
    expr = "status IN ('pending', 'accepted', 'rejected', 'expired')"
  }

  index "workspace_invitations_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "workspace_invitations_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "workspace_invitations_email_idx" {
    columns = [column.email]
  }

  index "workspace_invitations_status_idx" {
    columns = [column.status]
  }

  foreign_key "workspace_invitations_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "workspace_invitations_invited_by_fkey" {
    columns     = [column.invited_by]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
}