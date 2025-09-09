-- More efficient transformation using JSONB aggregation
CREATE OR REPLACE FUNCTION transform_zip_data_to_object_efficient(zip_data jsonb)
RETURNS jsonb AS $$
BEGIN
    RETURN jsonb_build_object(
        'schema', jsonb_build_array('population', 'latitude', 'longitude', 'workspace-id', 'status'),
        'data', (
            SELECT jsonb_object_agg(
                entry->>0,  -- zip code as key
                jsonb_build_array(entry->1, entry->2, entry->3, entry->4, entry->5)  -- values array
            )
            FROM jsonb_array_elements(zip_data->'data') AS entry
        )
    );
END;
$$ LANGUAGE plpgsql;

-- Test with small sample first
SELECT transform_zip_data_to_object_efficient('{"schema": ["zip-code", "population", "lat", "lng", "workspace", "status"], "data": [["90210", 21741, 34.1, -118.4, null, "e"], ["94102", 31176, 37.8, -122.4, "ws-id", "r"]]}'::jsonb);