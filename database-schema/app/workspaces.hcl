table "workspaces" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }

  column "name" {
    null = false
    type = varchar(255)
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

  index "workspaces_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "workspaces_name_idx" {
    columns = [column.name]
  }
}