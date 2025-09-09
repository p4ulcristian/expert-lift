table "zip_codes" {
  schema = schema.public

  column "id" {
    null = false
    type = integer
    default = 1
  }

  column "zip_codes" {
    null = true
    type = jsonb
  }

  column "created_at" {
    null = true
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  column "updated_at" {
    null = true
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }

  primary_key {
    columns = [column.id]
  }
}