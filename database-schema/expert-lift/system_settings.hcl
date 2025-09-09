table "system_settings" {
  schema = schema.expert_lift
  
  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }
  
  column "setting_key" {
    null = false
    type = varchar(100)
  }
  
  column "setting_value" {
    null = true
    type = text
  }
  
  column "description" {
    null = true
    type = varchar(255)
  }
  
  column "updated_by_user_id" {
    null = false
    type = uuid
  }
  
  column "updated_at" {
    null = false
    type = timestamptz
    default = sql("now()")
  }
  
  primary_key {
    columns = [column.id]
  }
  
  index "system_settings_setting_key_idx" {
    unique = true
    columns = [column.setting_key]
  }
  
  foreign_key "system_settings_updated_by_user_id_fkey" {
    columns     = [column.updated_by_user_id]
    ref_columns = [table.users.column.id]
    on_delete   = RESTRICT
  }
}