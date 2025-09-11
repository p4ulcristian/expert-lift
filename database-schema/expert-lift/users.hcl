table "users" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "username" {
    null = false
    type = varchar(100)
  }
  
  column "full_name" {
    null = false
    type = varchar(200)
  }
  
  column "password_hash" {
    null = false
    type = varchar(255)
  }
  
  column "role" {
    null = false
    type = enum.user_role
  }
  
  column "email" {
    null = true
    type = varchar(255)
  }
  
  column "phone" {
    null = true
    type = varchar(50)
  }
  
  column "active" {
    null = false
    type = boolean
    default = true
  }
  
  column "created_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  column "workspace_id" {
    null = true
    type = uuid
  }

  column "updated_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  primary_key {
    columns = [column.id]
  }
  
  foreign_key "workspace_fk" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = CASCADE
    on_delete   = SET_NULL
  }
  
  index "users_username_idx" {
    unique = true
    columns = [column.username]
  }
  
  index "users_email_idx" {
    unique = true
    columns = [column.email]
    where = "email IS NOT NULL"
  }
  
  index "users_full_name_idx" {
    columns = [column.full_name]
  }
}

enum "user_role" {
  schema = schema.expert_lift
  values = ["employee", "admin", "superadmin"]
}

schema "expert_lift" {
  comment = "Expert Lift elevator service management system"
}