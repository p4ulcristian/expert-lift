table "addresses" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "name" {
    null = false
    type = varchar(200)
    comment = "Building or company name"
  }
  
  column "address_line1" {
    null = false
    type = varchar(255)
  }
  
  column "address_line2" {
    null = true
    type = varchar(255)
  }
  
  column "city" {
    null = false
    type = varchar(100)
  }
  
  column "postal_code" {
    null = false
    type = varchar(20)
  }
  
  column "country" {
    null = false
    type = varchar(100)
    default = "Hungary"
  }
  
  column "contact_person" {
    null = true
    type = varchar(200)
  }
  
  column "contact_phone" {
    null = true
    type = varchar(50)
  }
  
  column "contact_email" {
    null = true
    type = varchar(255)
  }
  
  column "created_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  column "updated_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  primary_key {
    columns = [column.id]
  }
  
  index "addresses_name_idx" {
    columns = [column.name]
  }
  
  index "addresses_postal_code_idx" {
    columns = [column.postal_code]
  }
  
  index "addresses_city_idx" {
    columns = [column.city]
  }
}