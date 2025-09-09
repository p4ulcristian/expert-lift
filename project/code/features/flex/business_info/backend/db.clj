(ns features.flex.business-info.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-business-info-by-workspace
  "Get business info for a workspace"
  [{:keys [workspace-id]}]
  (first
    (postgres/execute-sql
      "SELECT 
        bi.id,
        bi.workspace_id,
        bi.business_name,
        bi.owner_name,
        bi.phone_number,
        bi.email_address,
        bi.mailing_address_line,
        bi.mailing_city,
        bi.mailing_state,
        bi.mailing_zip,
        bi.facility_address_line,
        bi.facility_city,
        bi.facility_state,
        bi.facility_zip,
        bi.created_at,
        bi.updated_at
      FROM business_info bi
      WHERE bi.workspace_id = $1"
      {:params [workspace-id]})))

(defn upsert-business-info
  "Create or update business info for a workspace"
  [{:keys [id workspace-id business-name owner-name phone-number email-address
           mailing-address-line mailing-city mailing-state mailing-zip
           facility-address-line facility-city facility-state facility-zip]}]
  (first
    (postgres/execute-sql
      "INSERT INTO business_info (
        id,
        workspace_id,
        business_name,
        owner_name,
        phone_number,
        email_address,
        mailing_address_line,
        mailing_city,
        mailing_state,
        mailing_zip,
        facility_address_line,
        facility_city,
        facility_state,
        facility_zip,
        created_at,
        updated_at
      ) VALUES (
        COALESCE($1, gen_random_uuid()),
        $2,
        $3,
        $4,
        $5,
        $6,
        $7,
        $8,
        $9,
        $10,
        $11,
        $12,
        $13,
        $14,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
      )
      ON CONFLICT (workspace_id) 
      DO UPDATE SET
        business_name = EXCLUDED.business_name,
        owner_name = EXCLUDED.owner_name,
        phone_number = EXCLUDED.phone_number,
        email_address = EXCLUDED.email_address,
        mailing_address_line = EXCLUDED.mailing_address_line,
        mailing_city = EXCLUDED.mailing_city,
        mailing_state = EXCLUDED.mailing_state,
        mailing_zip = EXCLUDED.mailing_zip,
        facility_address_line = EXCLUDED.facility_address_line,
        facility_city = EXCLUDED.facility_city,
        facility_state = EXCLUDED.facility_state,
        facility_zip = EXCLUDED.facility_zip,
        updated_at = CURRENT_TIMESTAMP
      RETURNING id, workspace_id, business_name, owner_name, phone_number, email_address,
        mailing_address_line, mailing_city, mailing_state, mailing_zip,
        facility_address_line, facility_city, facility_state, facility_zip,
        created_at, updated_at"
      {:params [id workspace-id business-name owner-name phone-number email-address
                mailing-address-line mailing-city mailing-state mailing-zip
                facility-address-line facility-city facility-state facility-zip]})))