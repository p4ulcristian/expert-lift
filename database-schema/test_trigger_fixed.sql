-- Update the trigger function to use proper JSON structure
CREATE OR REPLACE FUNCTION update_row_2_on_row_1_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Only trigger if row id=1 was modified
    IF NEW.id = 1 THEN
        -- Update row id=2 with proper JSON structure in zip_codes JSONB column
        UPDATE zip_codes 
        SET 
            zip_codes = COALESCE(zip_codes, '{}'::jsonb) || jsonb_build_object(
                'trigger_update', jsonb_build_object(
                    'random_id', substring(gen_random_uuid()::text from 1 for 8),
                    'timestamp', extract(epoch from now()),
                    'triggered_by', 'row_1_update'
                )
            ),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = 2;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Test the updated trigger
UPDATE zip_codes SET zip_codes = '{"test": "modified row 1 again", "timestamp": "' || now() || '"}' WHERE id = 1;

-- Show the updated JSON structure in row 2
SELECT id, jsonb_pretty(zip_codes) as zip_codes_formatted FROM zip_codes WHERE id = 2;