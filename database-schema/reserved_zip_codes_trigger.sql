-- Create trigger function to filter reserved zip codes into row 2
CREATE OR REPLACE FUNCTION update_row_2_with_reserved_zip_codes()
RETURNS TRIGGER AS $$
DECLARE
    filtered_data jsonb;
    schema_array jsonb;
    reserved_entries jsonb := '[]'::jsonb;
    entry jsonb;
BEGIN
    -- Only trigger if row id=1 was modified
    IF NEW.id = 1 THEN
        -- Get the schema from row 1
        schema_array := NEW.zip_codes->'schema';
        
        -- Filter the data array to only include entries where status = "r" (reserved)
        -- Loop through each entry in the data array
        FOR entry IN SELECT jsonb_array_elements(NEW.zip_codes->'data')
        LOOP
            -- Check if status (index 5) is "r" for reserved
            IF entry->5 = '"r"'::jsonb THEN
                reserved_entries := reserved_entries || entry;
            END IF;
        END LOOP;
        
        -- Create the same structure as row 1 but with only reserved entries
        filtered_data := jsonb_build_object(
            'schema', schema_array,
            'data', reserved_entries
        );
        
        -- Update row id=2 with the filtered structure
        UPDATE zip_codes 
        SET 
            zip_codes = filtered_data,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = 2;
        
        -- Log the number of reserved entries found
        RAISE NOTICE 'Updated row 2 with % reserved zip codes', jsonb_array_length(reserved_entries);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop existing trigger and create the new one
DROP TRIGGER IF EXISTS zip_codes_row_1_update_trigger ON zip_codes;
CREATE TRIGGER zip_codes_row_1_update_trigger
    AFTER UPDATE ON zip_codes
    FOR EACH ROW
    EXECUTE FUNCTION update_row_2_with_reserved_zip_codes();

-- Test the trigger by making a small update to row 1
UPDATE zip_codes 
SET zip_codes = zip_codes || jsonb_build_object('last_updated', extract(epoch from now())) 
WHERE id = 1;