table "worksheets" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "serial_number" {
    null = false
    type = varchar(50)
    comment = "Format: YYYY-MM-DD/001"
  }
  
  column "creation_date" {
    null = false
    type = date
  }
  
  column "arrival_time" {
    null = true
    type = timestamptz
  }
  
  column "departure_time" {
    null = true
    type = timestamptz
  }
  
  column "work_duration_hours" {
    null = true
    type = decimal(4,2)
    comment = "Auto-calculated from arrival/departure, rounded up"
  }
  
  column "work_type" {
    null = false
    type = enum.work_type
  }
  
  column "service_type" {
    null = false
    type = enum.service_type
  }
  
  column "work_description" {
    null = false
    type = text
  }
  
  column "material_usage" {
    null = true
    type = jsonb
    comment = "Array of {name, unit, quantity} objects e.g. [{\"name\": \"acel\", \"unit\": \"meter\", \"quantity\": \"5\"}]"
  }
  
  column "notes" {
    null = true
    type = text
  }
  
  column "status" {
    null = false
    type = enum.worksheet_status
    default = "draft"
  }
  
  column "pdf_file_path" {
    null = true
    type = varchar(500)
  }
  
  column "maintainer_signature" {
    null = true
    type = text
    comment = "Base64 encoded maintainer signature data URL"
  }
  
  column "customer_signature" {
    null = true
    type = text
    comment = "Base64 encoded customer signature data URL"
  }
  
  column "created_by_user_id" {
    null = false
    type = uuid
  }
  
  column "assigned_to_user_id" {
    null = true
    type = uuid
  }
  
  column "address_id" {
    null = false
    type = uuid
  }
  
  column "elevator_id" {
    null = true
    type = uuid
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
  
  primary_key {
    columns = [column.id]
  }
  
  index "worksheets_serial_number_idx" {
    unique = true
    columns = [column.serial_number]
  }
  
  index "worksheets_creation_date_idx" {
    columns = [column.creation_date]
  }
  
  index "worksheets_status_idx" {
    columns = [column.status]
  }
  
  index "worksheets_created_by_user_id_idx" {
    columns = [column.created_by_user_id]
  }
  
  index "worksheets_assigned_to_user_id_idx" {
    columns = [column.assigned_to_user_id]
  }
  
  index "worksheets_address_id_idx" {
    columns = [column.address_id]
  }
  
  foreign_key "worksheets_created_by_user_id_fkey" {
    columns     = [column.created_by_user_id]
    ref_columns = [table.users.column.id]
    on_delete   = RESTRICT
  }
  
  foreign_key "worksheets_assigned_to_user_id_fkey" {
    columns     = [column.assigned_to_user_id]
    ref_columns = [table.users.column.id]
    on_delete   = SET_NULL
  }
  
  foreign_key "worksheets_address_id_fkey" {
    columns     = [column.address_id]
    ref_columns = [table.addresses.column.id]
    on_delete   = CASCADE
  }
  
  foreign_key "worksheets_elevator_id_fkey" {
    columns     = [column.elevator_id]
    ref_columns = [table.elevators.column.id]
    on_delete   = SET_NULL
  }
}

enum "work_type" {
  schema = schema.expert_lift
  values = ["repair", "maintenance", "other"]
}

enum "service_type" {
  schema = schema.expert_lift
  values = ["normal", "night", "weekend", "holiday"]
}

enum "worksheet_status" {
  schema = schema.expert_lift
  values = ["draft", "in_progress", "completed", "cancelled"]
}