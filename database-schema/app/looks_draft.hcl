table "looks_draft" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }

  column "name" {
    null = false
    type = varchar(255)
  }

  column "price_group_key" {
    null = true
    type = varchar(255)
  }

  column "tags" {
    null = true
    type = sql("varchar(255)[]")
  }

  column "texture" {
    null = false
    type = jsonb
  }

  column "basecolor" {
    null = true
    type = varchar(255)
  }

  column "color_family" {
    null = true
    type = varchar(255)
  }

  column "layers" {
    null = true
    type = sql("jsonb[]")
  }

  column "layers_count" {
    null = true
    type = integer
  }

  primary_key {
    columns = [column.id]
  }

  index "looks_draft_name_idx" {
    unique  = true
    columns = [column.name]
  }

  index "looks_draft_family_idx" {
    columns = [column.color_family]
  }
} 