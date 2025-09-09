table "locations" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "name" {
    null = false
    type = text
  }

  column "description" {
    null = true
    type = text
    default = ""
  }

  column "type" {
    null = false
    type = text
    default = "workstation"
  }

  column "status" {
    null = false
    type = text
    default = "active"
  }

  column "capacity" {
    null = true
    type = integer
  }

  column "tags" {
    null = false
    type = jsonb
    default = "[]"
  }

  column "linked_operators" {
    null = false
    type = jsonb
    default = "[]"
  }

  column "workstation_processes" {
    null = false
    type = jsonb
    default = "[]"
  }

  column "is_partner_location" {
    null = false
    type = boolean
    default = false
  }

  column "geo_info" {
    null = true
    type = text
  }

  column "notes" {
    null = true
    type = text
    default = ""
  }

  column "workspace_id" {
    null = true
    type = uuid
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

  foreign_key "locations_workspace_id_fkey" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = CASCADE
    on_delete   = CASCADE
  }

  index "locations_workspace_id_idx" {
    columns = [column.workspace_id]
  }

  index "locations_type_idx" {
    columns = [column.type]
  }

  index "locations_status_idx" {
    columns = [column.status]
  }

  index "locations_is_partner_location_idx" {
    columns = [column.is_partner_location]
  }

  index "locations_tags_gin_idx" {
    columns = [column.tags]
    type    = GIN
  }

  check "locations_type_check" {
    expr = "type IN ('workstation', 'rack', 'area', 'partner', 'inbound')"
  }

  check "locations_status_check" {
    expr = "status IN ('active', 'idle', 'over-capacity', 'has-alerts')"
  }
} 