function "normalize_address_search" {
  schema = schema.expert_lift
  lang   = PLpgSQL
  body = <<-EOF
  BEGIN
      NEW.search_normalized := translate(
          lower(
              COALESCE(NEW.name, '') || ' ' ||
              COALESCE(NEW.address_line1, '') || ' ' ||
              COALESCE(NEW.address_line2, '') || ' ' ||
              COALESCE(NEW.city, '') || ' ' ||
              COALESCE(NEW.country, '') || ' ' ||
              COALESCE(NEW.contact_person, '')
          ),
          'áéíóöőúüű',
          'aeiooouuu'
      );
      RETURN NEW;
  END;
  EOF
  return = trigger
  comment = "Normalize Hungarian characters in address search text"
}

trigger "update_address_search_normalized" {
  schema = schema.expert_lift
  table  = table.addresses
  name   = "update_address_search_normalized"
  for    = [ROW]
  before = true
  on     = [INSERT, UPDATE]
  function {
    name = function.normalize_address_search
  }
  comment = "Automatically update search_normalized column on address changes"
}