table "looks" {
  schema = schema.public
  column "id" {
    type = uuid
    null = false
  }
  column "price_group_key" {
    type = varchar(255)
    null = false
  }
  column "thumbnail" {
    type = varchar(2048)
    null = true
  }
  column "tags" {
    type = sql("varchar(255)[]")
    null = true
  }
  column "texture" {
    type = jsonb
    null = false
  }
  column "name" {
    type = varchar(255)
    null = false
  }
  column "basecolor" {
    type = varchar(255)
    null = false
  }
  column "color_family" {
    type = varchar(255)
    null = true
  }
  column "layers" {
    type = sql("jsonb[]")
    null = true
  }
  column "layers_count" {
    type = integer
    null = true
  }
  column "created_at" {
    type = timestamptz
    null = false
    default = sql("now()")
  }

  index "looks_id_idx" {
    unique  = true
    columns = [column.id]
  }

  index "looks_name_idx" {
    unique  = true
    columns = [column.name]
  }

  index "looks_family_idx" {
    columns = [column.color_family]
  }
}