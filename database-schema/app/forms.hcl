table "forms" {
  schema = schema.public
  column "id" {
    type = uuid
    null = false
    default = sql("gen_random_uuid()")
  }
  column "title" {
    type = varchar(255)
    null = false
  }
  column "template" {
    type = jsonb
    null = false
  }
  column "price_formula" {
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

  index "forms_id_idx" {
    unique = true
    columns = [column.id]
  }
  
  index "forms_title_idx" {
    columns = [column.title]
  }
  
  index "forms_price_formula_idx" {
    columns = [column.price_formula]
  }
} 