table "business_info" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "workspace_id" {
    null = false
    type = uuid
  }

  column "business_name" {
    null = true
    type = varchar(255)
  }

  column "owner_name" {
    null = true
    type = varchar(255)
  }

  column "phone_number" {
    null = true
    type = varchar(50)
  }

  column "email_address" {
    null = true
    type = varchar(255)
  }

  // Mailing address fields
  column "mailing_address_line" {
    null = true
    type = varchar(255)
  }

  column "mailing_city" {
    null = true
    type = varchar(100)
  }

  column "mailing_state" {
    null = true
    type = varchar(50)
  }

  column "mailing_zip" {
    null = true
    type = varchar(20)
  }

  // Facility address fields
  column "facility_address_line" {
    null = true
    type = varchar(255)
  }

  column "facility_city" {
    null = true
    type = varchar(100)
  }

  column "facility_state" {
    null = true
    type = varchar(50)
  }

  column "facility_zip" {
    null = true
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

  primary_key {
    columns = [column.id]
  }

  foreign_key "business_info_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = CASCADE
    on_delete   = CASCADE
  }

  index "business_info_workspace_id_idx" {
    unique  = true
    columns = [column.workspace_id]
  }

  index "business_info_id_idx" {
    columns = [column.id]
  }
}