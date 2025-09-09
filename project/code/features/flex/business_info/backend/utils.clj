(ns features.flex.business-info.backend.utils)

(defn timestamp-to-string 
  "Convert timestamp to ISO string for JSON serialization"
  [timestamp]
  (when timestamp
    (str timestamp)))

(defn transform-business-info-to-db
  "Transform frontend format to database record"
  [workspace-id business-info]
  {:id nil
   :workspace-id workspace-id
   :business-name (:business-name business-info)
   :owner-name (:owner-name business-info)
   :phone-number (:phone-number business-info) 
   :email-address (:email-address business-info)
   :mailing-address-line (get-in business-info [:mailing-address :address])
   :mailing-city (get-in business-info [:mailing-address :city])
   :mailing-state (get-in business-info [:mailing-address :state])
   :mailing-zip (get-in business-info [:mailing-address :zip-code])
   :facility-address-line (get-in business-info [:facility-address :address])
   :facility-city (get-in business-info [:facility-address :city])
   :facility-state (get-in business-info [:facility-address :state])
   :facility-zip (get-in business-info [:facility-address :zip-code])})

(defn transform-business-info-from-db
  "Transform database record to frontend format"
  [db-record]
  (when db-record
    {:business-name (:business_name db-record)
     :owner-name (:owner_name db-record)
     :phone-number (:phone_number db-record)
     :email-address (:email_address db-record)
     :mailing-address {:address (:mailing_address_line db-record)
                       :city (:mailing_city db-record)
                       :state (:mailing_state db-record)
                       :zip-code (:mailing_zip db-record)}
     :facility-address {:address (:facility_address_line db-record)
                        :city (:facility_city db-record)
                        :state (:facility_state db-record)
                        :zip-code (:facility_zip db-record)}
     :created-at (timestamp-to-string (:created_at db-record))
     :updated-at (timestamp-to-string (:updated_at db-record))}))