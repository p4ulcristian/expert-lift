table "worksheet_attachments" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "worksheet_id" {
    null = false
    type = uuid
  }
  
  column "file_name" {
    null = false
    type = varchar(255)
  }
  
  column "file_path" {
    null = false
    type = varchar(500)
  }
  
  column "file_type" {
    null = false
    type = enum.file_type
  }
  
  column "file_size_bytes" {
    null = false
    type = integer
  }
  
  column "uploaded_by_user_id" {
    null = false
    type = uuid
  }
  
  column "uploaded_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  primary_key {
    columns = [column.id]
  }
  
  index "worksheet_attachments_worksheet_id_idx" {
    columns = [column.worksheet_id]
  }
  
  index "worksheet_attachments_uploaded_by_user_id_idx" {
    columns = [column.uploaded_by_user_id]
  }
  
  foreign_key "worksheet_attachments_worksheet_id_fkey" {
    columns     = [column.worksheet_id]
    ref_columns = [table.worksheets.column.id]
    on_delete   = CASCADE
  }
  
  foreign_key "worksheet_attachments_uploaded_by_user_id_fkey" {
    columns     = [column.uploaded_by_user_id]
    ref_columns = [table.users.column.id]
    on_delete   = RESTRICT
  }
}

enum "file_type" {
  schema = schema.expert_lift
  values = ["image", "document", "pdf"]
}