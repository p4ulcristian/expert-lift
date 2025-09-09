table "recipe_processes" {
  schema = schema.public

  column "id" {
    null = false
    type = serial
  }

  column "recipe_id" {
    type = uuid
  }

  column "process_id" {
    type = uuid
  }

  column "step_order" {
    type = integer
  }

  column "created_at" {
    type = timestamp
    default = sql("CURRENT_TIMESTAMP")
  }

  primary_key {
    columns = [column.id]
  }

  index "recipe_processes_recipe_id_idx" {
    columns = [column.recipe_id]
  }

  index "recipe_processes_process_id_idx" {
    columns = [column.process_id]
  }

  foreign_key "recipe_processes_recipe_id_fkey" {
    columns = [column.recipe_id]
    ref_columns = [table.recipes.column.id]
    on_delete = CASCADE
  }

  foreign_key "recipe_processes_process_id_fkey" {
    columns = [column.process_id]
    ref_columns = [table.processes.column.id]
    on_delete = CASCADE
  }
} 