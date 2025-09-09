table "parts" {
  schema = schema.public

  column "id" {
    type = uuid
    null = false
    default = sql("gen_random_uuid()")
  }

  column "name" {
    type = varchar(255)
    null = true
  }

  column "description" {
    type = text
    null = true
  }

  column "picture_url" {
    type = text
    null = true
  }

  column "package_id" {
    type = uuid
    null = true
  }

  column "mesh_id" {
    type = varchar(255)
    null = true
  }

  column "created_at" {
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  column "updated_at" {
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  column "popular" {
    type = boolean
    null = false
    default = false
  }

  column "form_id" {
    type = uuid
    null = true
  }

  column "order_position" {
    type = integer
    null = false
    default = 0
  }

  index "parts_id_idx" {
    unique = true
    columns = [column.id]
  }

  index "parts_package_id_idx" {
    columns = [column.package_id]
  }

  index "parts_form_id_idx" {
    columns = [column.form_id]
  }

  foreign_key "fk_parts_form" {
    columns     = [column.form_id]
    ref_columns = [table.forms.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

}