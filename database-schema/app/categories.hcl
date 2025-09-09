table "categories" {
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
  
  column "category_id" {
    type = uuid
    null = true
  }
  
  column "created_at" {
    type = timestamptz
    default = sql("CURRENT_TIMESTAMP")
  }
  
  column "order_position" {
    type = int
    null = true
    default = 0
  }

  index "categories_id_idx" {
    unique = true
    columns = [column.id]
  }

  index "categories_category_id_idx" {
    columns = [column.category_id]
  }
} 