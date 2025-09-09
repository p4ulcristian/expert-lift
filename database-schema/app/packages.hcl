table "packages" {
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

  column "prefix" {
    type = varchar(50)
    null = true
  }

  column "category_id" {
    type = uuid
    null = true
  }

  column "model_url" {
    type = text
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
    type = int
    null = true
    default = 0
  }

  index "packages_id_idx" {
    unique = true
    columns = [column.id]
  }

  index "packages_category_id_idx" {
    columns = [column.category_id]
  }

  index "packages_form_id_idx" {
    columns = [column.form_id]
  }

  foreign_key "fk_packages_form" {
    columns     = [column.form_id]
    ref_columns = [table.forms.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "fk_packages_category" {
    columns     = [column.category_id]
    ref_columns = [table.categories.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
}