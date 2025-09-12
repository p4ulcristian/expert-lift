table "addresses" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "name" {
    null = false
    type = varchar(200)
    comment = "Building or company name"
  }
  
  column "address_line1" {
    null = false
    type = varchar(255)
  }
  
  column "address_line2" {
    null = true
    type = varchar(255)
  }
  
  column "city" {
    null = false
    type = varchar(100)
  }
  
  column "postal_code" {
    null = false
    type = varchar(20)
  }
  
  column "country" {
    null = false
    type = varchar(100)
    default = "Hungary"
  }
  
  column "contact_person" {
    null = true
    type = varchar(200)
  }
  
  column "contact_phone" {
    null = true
    type = varchar(50)
  }
  
  column "contact_email" {
    null = true
    type = varchar(255)
  }

  column "elevators" {
    null = true
    type = jsonb
    comment = "Felvonok - Array of elevator identifiers (Felvono jelzese)"
  }

  column "search_normalized" {
    null = true
    type = text
    comment = "Normalized search text for Hungarian character matching"
  }
  
  column "created_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  column "updated_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }

  column "workspace_id" {
    null = false
    type = uuid
  }
  
  primary_key {
    columns = [column.id]
  }

  foreign_key "addresses_workspace_fk" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = CASCADE
    on_delete   = CASCADE
  }
  
  index "addresses_name_idx" {
    columns = [column.name]
  }
  
  index "addresses_postal_code_idx" {
    columns = [column.postal_code]
  }
  
  index "addresses_city_idx" {
    columns = [column.city]
  }
  
  index "addresses_search_normalized_idx" {
    columns = [column.search_normalized]
    comment = "Index for fast LIKE searches on normalized content"
  }
}