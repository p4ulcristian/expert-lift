table "services" {
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
  }

  column "picture_url" {
    null = true
    type = text
  }

  index "services_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "services_name_idx" {
    unique  = true
    columns = [column.name]
  }
} 