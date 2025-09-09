-- Update trigger function to work with object format instead of array format

CREATE OR REPLACE FUNCTION update_row_2_with_reserved_zip_codes()
RETURNS TRIGGER AS $$
DECLARE
    filtered_data jsonb;
    schema_array jsonb;
    reserved_data jsonb := '{}'::jsonb;
    zip_code text;
    zip_values jsonb;
    reserved_count integer := 0;
BEGIN
    -- Only trigger if row id=1 was modified
    IF NEW.id = 1 THEN
        -- Get schema
        schema_array := NEW.zip_codes->'schema';
        
        -- Loop through each zip code in the object
        FOR zip_code IN SELECT jsonb_object_keys(NEW.zip_codes->'data')
        LOOP
            zip_values := NEW.zip_codes->'data'->zip_code;
            
            -- Check if status (index 4) is "r" for reserved
            IF zip_values->>4 = 'r' THEN
                reserved_data := jsonb_set(reserved_data, array[zip_code], zip_values);
                reserved_count := reserved_count + 1;
            END IF;
        END LOOP;
        
        -- Create final structure with same object format as row 1
        filtered_data := jsonb_build_object(
            'schema', schema_array,
            'data', reserved_data
        );
        
        -- Update row 2 with filtered reserved zip codes
        UPDATE zip_codes 
        SET zip_codes = filtered_data, updated_at = CURRENT_TIMESTAMP
        WHERE id = 2;
        
        RAISE NOTICE 'Updated row 2 with % reserved zip codes in object format', reserved_count;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;