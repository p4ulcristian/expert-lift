table "elevators" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "elevator_code" {
    null = false
    type = varchar(100)
    comment = "Internal identifier for the elevator"
  }
  
  column "manufacturer" {
    null = true
    type = varchar(100)
  }
  
  column "model" {
    null = true
    type = varchar(100)
  }
  
  column "year_installed" {
    null = true
    type = integer
  }
  
  column "floors_served" {
    null = true
    type = integer
  }
  
  column "capacity_kg" {
    null = true
    type = integer
  }
  
  column "address_id" {
    null = false
    type = uuid
  }
  
  column "notes" {
    null = true
    type = text
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
  
  column "updated_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  primary_key {
    columns = [column.id]
  }
  
  index "elevators_elevator_code_idx" {
    unique = true
    columns = [column.elevator_code]
  }
  
  index "elevators_address_id_idx" {
    columns = [column.address_id]
  }
  
  foreign_key "elevators_address_id_fkey" {
    columns     = [column.address_id]
    ref_columns = [table.addresses.column.id]
    on_delete   = CASCADE
  }
}