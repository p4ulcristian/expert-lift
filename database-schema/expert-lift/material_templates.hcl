table "material_templates" {
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
  
  column "unit" {
    null = false
    type = varchar(20)
    comment = "Units like 'pcs', 'm', 'kg', 'l', etc."
  }
  
  column "category" {
    null = true
    type = varchar(100)
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

  column "workspace_id" {
    null = false
    type = uuid
  }
  
  primary_key {
    columns = [column.id]
  }

  foreign_key "material_templates_workspace_fk" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = CASCADE
    on_delete   = CASCADE
  }
  
  index "material_templates_name_idx" {
    columns = [column.name]
  }
  
  index "material_templates_category_idx" {
    columns = [column.category]
  }
}