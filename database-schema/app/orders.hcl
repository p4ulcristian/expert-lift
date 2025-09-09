table "orders" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
    default = sql("gen_random_uuid()")
  }

   column "workspace_id" {
    null = true
    type = uuid
  }

  column "user_id" {
    null = true
    type = uuid
  }

  column "created_at" {
    null = true
    type = timestamp
    default = sql("now()")
  }

  column "due_date" {
    null = true
    type = timestamp
  }

  column "status" {
    null = true
    type = text
  }

  column "urgency" {
    null = true
    type = text
  }

  column "source" {
    null = true
    type = text
  }

  column "total_amount" {
    type = decimal(10,2)
    null = true
    default = 0.00
  }

  column "payment_intent_id" {
    null = true
    type = text
  }

  column "updated_at" {
    null = true
    type = timestamp
    default = sql("now()")
  }

  column "payment_status" {
    null = true
    type = text
    default = "unpaid"
  }

  check "status_valid" {
    expr = "status IN ('order-submitted', 'package-arrived', 'parts-inspected', 'waiting-to-start', 'process-planning', 'batch-assigned', 'in-progress', 'job-paused-on-hold', 'job-inspected', 'job-complete', 'packing', 'outbound-shipping-ordered', 'sent-to-customer', 'arrived-at-customer', 'customer-accepted-order', 'attention-needed', 'invoice-issued', 'invoice-sent', 'invoice-paid', 'awaiting-customer-response', 'awaiting-parts', 'rework-required', 'cancelled', 'declined', 'quote-sent', 'ready-to-transport')"
  }

  check "payment_status_valid" {
    expr = "payment_status IN ('unpaid', 'pending', 'paid', 'failed', 'refunded', 'cancelled')"
  }

  check "urgency_valid" {
    expr = "urgency IN ('low', 'normal', 'high', 'critical', 'rush')"
  }

  check "source_valid" {
    expr = "source IN ('iron-rainbow', 'local')"
  }

  index "orders_id_idx" {
    unique  = true
    columns = [column.id]
  }

  foreign_key "fk_orders_workspace" {
    columns     = [column.workspace_id]
    ref_columns = [table.workspaces.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }

  foreign_key "fk_orders_user" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = SET_NULL
  }

}