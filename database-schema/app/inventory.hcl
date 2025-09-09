table "inventory" {
  schema = schema.public

  column "id" {
    type = uuid
    null = false
    default = sql("gen_random_uuid()")
  }

  column "name" {
    type = varchar(255)
    null = false
  }

  column "description" {
    type = text
    null = true
  }

  column "category" {
    type = varchar(100)
    null = true
  }

  column "type" {
    type = varchar(100)
    null = true
  }

  column "quantity" {
    type = decimal(10,2)
    null = false
    default = 0
  }

  column "min_qty" {
    type = decimal(10,2)
    null = false
    default = 0
  }

  column "unit" {
    type = varchar(20)
    null = true
    default = "pcs"
  }

  column "supplier" {
    type = varchar(255)
    null = true
  }

  column "item_category" {
    type = varchar(50)
    null = true
  }

  column "picture_url" {
    type = text
    null = true
  }

  column "workspace_id" {
    type = uuid
    null = false
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

  index "inventory_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "inventory_category_idx" {
    columns = [column.category]
  }

  index "inventory_type_idx" {
    columns = [column.type]
  }

  index "inventory_supplier_idx" {
    columns = [column.supplier]
  }

  index "inventory_quantity_idx" {
    columns = [column.quantity]
  }

  index "inventory_low_stock_idx" {
    columns = [column.workspace_id, column.quantity, column.min_qty]
  }

  foreign_key "inventory_workspace_id_fkey" {
    columns = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_delete = CASCADE
  }

  check "inventory_quantity_positive" {
    expr = "quantity >= 0"
  }

  check "inventory_min_qty_positive" {
    expr = "min_qty >= 0"
  }

  check "inventory_item_category_check" {
    expr = "item_category IN ('consumable', 'spare', 'tool', 'material', 'chemical', 'component', 'equipment')"
  }
} 