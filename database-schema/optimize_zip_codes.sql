-- Optimize zip codes lookup performance by transforming to object format
-- This changes from O(n) array search to O(1) object key lookup

-- Step 1: Create transformation function
CREATE OR REPLACE FUNCTION transform_zip_data_to_object(zip_data jsonb)
RETURNS jsonb AS $$
DECLARE
    result jsonb := '{"data": {}, "schema": []}'::jsonb;
    schema_info jsonb;
    data_array jsonb;
    zip_entry jsonb;
    zip_code text;
    zip_values jsonb;
BEGIN
    -- Extract schema and data
    schema_info := zip_data->'schema';
    data_array := zip_data->'data';
    
    -- Set schema (remove zip-code from schema since it becomes the key)
    result := jsonb_set(result, '{schema}', 
        jsonb_build_array('population', 'latitude', 'longitude', 'workspace-id', 'status'));
    
    -- Transform each zip code entry
    FOR zip_entry IN SELECT jsonb_array_elements(data_array)
    LOOP
        zip_code := zip_entry->>0;  -- Extract zip code
        -- Create array with remaining values (population, lat, lng, workspace, status)
        zip_values := jsonb_build_array(
            zip_entry->1,  -- population
            zip_entry->2,  -- latitude  
            zip_entry->3,  -- longitude
            zip_entry->4,  -- workspace-id
            zip_entry->5   -- status
        );
        
        -- Add to result object with zip code as key
        result := jsonb_set(result, array['data', zip_code], zip_values);
    END LOOP;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;