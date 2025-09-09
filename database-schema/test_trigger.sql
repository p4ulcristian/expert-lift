-- Create the trigger function
CREATE OR REPLACE FUNCTION update_row_2_on_row_1_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Only trigger if row id=1 was modified
    IF NEW.id = 1 THEN
        -- Update row id=2 with a random string in zip_codes JSONB column
        UPDATE zip_codes 
        SET 
            zip_codes = jsonb_build_object(
                'random_update', 
                'triggered_' || substring(gen_random_uuid()::text from 1 for 8) || '_' || extract(epoch from now())::text
            ),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = 2;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger
DROP TRIGGER IF EXISTS zip_codes_row_1_update_trigger ON zip_codes;
CREATE TRIGGER zip_codes_row_1_update_trigger
    AFTER UPDATE ON zip_codes
    FOR EACH ROW
    EXECUTE FUNCTION update_row_2_on_row_1_change();

-- Test data: ensure rows exist
INSERT INTO zip_codes (id, zip_codes) VALUES (1, '{"test": "row 1"}') ON CONFLICT (id) DO NOTHING;
INSERT INTO zip_codes (id, zip_codes) VALUES (2, '{"test": "row 2"}') ON CONFLICT (id) DO NOTHING;

-- Show current state
SELECT id, zip_codes, updated_at FROM zip_codes WHERE id IN (1, 2) ORDER BY id;