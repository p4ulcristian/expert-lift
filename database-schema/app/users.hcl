table "users" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }

  column "first_name" {
    null = false
    type = varchar(100)
  }

  column "last_name" {
    null = false
    type = varchar(100)
  }

  column "email" {
    null = false
    type = varchar(255)
  }


  column "picture_url" {
    null = true
    type = varchar(500)
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

  index "users_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "users_email_idx" {
    unique  = true
    columns = [column.email]
  }

}