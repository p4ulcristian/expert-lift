table "feedbacks" {
  schema = schema.expert_lift

  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }

  column "message" {
    null = false
    type = text
  }

  column "user_id" {
    null = false
    type = uuid
  }

  column "workspace_id" {
    null = false
    type = uuid
  }

  column "created_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }

  primary_key {
    columns = [column.id]
  }

  foreign_key "feedbacks_user_fk" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = CASCADE
    on_delete   = CASCADE
  }

  foreign_key "feedbacks_workspace_fk" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = CASCADE
    on_delete   = CASCADE
  }

  index "feedbacks_user_id_idx" {
    columns = [column.user_id]
  }

  index "feedbacks_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "feedbacks_created_at_idx" {
    columns = [column.created_at]
  }
}
