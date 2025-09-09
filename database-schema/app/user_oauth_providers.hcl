table "user_oauth_providers" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }

  column "user_id" {
    null = false
    type = uuid
  }

  column "oauth_id" {
    null = false
    type = varchar(255)
  }

  column "provider" {
    null = false
    type = varchar(50)
    default = "auth0"
  }

  column "created_at" {
    null = false
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  index "user_oauth_providers_id_idx" {
    unique = true
    columns = [column.id]
  }

  index "user_oauth_providers_oauth_id_idx" {
    unique = true
    columns = [column.oauth_id]
  }

  index "user_oauth_providers_user_id_idx" {
    columns = [column.user_id]
  }

  foreign_key "user_oauth_providers_user_id_fkey" {
    columns = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update = NO_ACTION
    on_delete = CASCADE
  }

  check "provider_valid" {
    expr = "provider IN ('auth0', 'google-oauth2', 'github', 'microsoft', 'apple', 'facebook', 'twitter')"
  }
}