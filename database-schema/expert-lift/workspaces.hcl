table "workspaces" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "name" {
    null = false
    type = varchar(200)
  }
  
  column "description" {
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
  
  index "workspaces_name_idx" {
    unique = true
    columns = [column.name]
  }
}