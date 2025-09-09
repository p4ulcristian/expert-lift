table "parts_pricing" {
  schema = schema.public

  column "id" {
    type = uuid
    null = false
    default = sql("gen_random_uuid()")
  }

  column "workspace_id" {
    type = uuid
    null = false
  }

  column "part_id" {
    type = uuid
    null = false
  }

  column "price_basic" {
    type = decimal(10,2)
    null = true
    default = 0.00
  }

  column "price_basic_plus" {
    type = decimal(10,2)
    null = true
    default = 0.00
  }

  column "price_pro" {
    type = decimal(10,2)
    null = true
    default = 0.00
  }

  column "price_pro_plus" {
    type = decimal(10,2)
    null = true
    default = 0.00
  }

  column "is_active" {
    type = boolean
    null = false
    default = true
  }

  column "created_at" {
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  column "updated_at" {
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  primary_key {
    columns = [column.id]
  }

  index "parts_pricing_id_idx" {
    unique = true
    columns = [column.id]
  }

  index "parts_pricing_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "parts_pricing_part_id_idx" {
    columns = [column.part_id]
  }

  index "parts_pricing_workspace_part_idx" {
    unique = true
    columns = [column.workspace_id, column.part_id]
  }

  index "parts_pricing_active_idx" {
    columns = [column.is_active]
  }

  foreign_key "parts_pricing_part_id_fkey" {
    columns     = [column.part_id]
    ref_columns = [table.parts.column.id]
    on_delete   = CASCADE
  }

  foreign_key "parts_pricing_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
} 