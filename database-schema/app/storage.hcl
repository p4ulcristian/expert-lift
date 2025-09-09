table "storage" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "alias" {
    null = false
    type = varchar(255)
  }

  column "type" {
    null = false
    type = varchar(255)
  }

  column "url" {
    null = true
    type = varchar(255)
  }

  column "size" {
    null = true
    type = bigint
  }
   column "mime_type" {
    null = true
    type = varchar(255)
  }

   column "path" {
    null = true
    type = sql("uuid[]")
  }
   column "items" {
    null = true
    type = sql("uuid[]")
  }
   column "wsid" {
    null = true
    type = uuid
  }
   column "added_by" {
    null = true
    type = uuid
  }
  column "added_at" {
    null = true
    type = timestamptz
  }

  index "storage_id_idx" {
    unique  = true
    columns = [column.id]
  }

  foreign_key "fk_storage_workspace" {
    columns     = [column.wsid]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
}