(ns features.common.stripe.backend.calculation
  (:import
   [com.stripe Stripe]
   [com.stripe.model.tax Calculation]
   [com.stripe.param.tax CalculationCreateParams 
                         CalculationCreateParams$LineItem
                         CalculationCreateParams$CustomerDetails
                         CalculationCreateParams$CustomerDetails$Address
                         CalculationCreateParams$CustomerDetails$AddressSource]))


(defn line-item-builder [options]
  (-> (CalculationCreateParams$LineItem/builder)
      (.setAmount    (:amount options))
      (.setReference "L1")
      (.setTaxCode   "txcd_10000000")
      .build))

(defn address-builder [{:keys [address]}]
  (-> (CalculationCreateParams$CustomerDetails$Address/builder)
      (.setLine1 (:line1 address))
      (.setCity (:city address))
      (.setState (:state address))
      (.setPostalCode (:postal-code address))
      (.setCountry "US")
      .build))

(defn customer-details-builder [options]
  (-> (CalculationCreateParams$CustomerDetails/builder)
      (.setAddress (address-builder options))
      (.setAddressSource CalculationCreateParams$CustomerDetails$AddressSource/SHIPPING)
      .build))

(defn create-tax-calculation-params [options]
  (println "calculate-tax" options)
  (-> (CalculationCreateParams/builder)
      (.setCurrency "usd")
      (.addLineItem (line-item-builder options))
      (.setCustomerDetails (customer-details-builder options))
      .build))

(defn create-calculation [options]
  (try
    (Calculation/create (create-tax-calculation-params options))
    (catch Exception e
      (println "Error creating calculation:" e)
      nil)))

(defn get-basic-tax-info [calculation]
  (try
    (let [total-amount  (.getAmountTotal calculation)
          exclusive-tax (.getTaxAmountExclusive calculation)
          inclusive-tax (.getTaxAmountInclusive calculation)
          total-tax     (+ exclusive-tax inclusive-tax)
          subtotal      (- total-amount exclusive-tax)
    
          tax-percentage (if (pos? subtotal)
                           (double (/ (* total-tax 100) subtotal))
                           0.0)]
      
           ;; Return the basic info
       {:total (/ total-amount 100.0) ;; Convert from cents to dollars
        :tax (/ total-tax 100.0)      ;; Convert from cents to dollars
        :subtotal (/ subtotal 100.0)  ;; Convert from cents to dollars
        :tax-percentage tax-percentage})
    (catch Exception e
      (println "Error getting basic tax info:" e)
      nil)))

;; -----------------------------------------------------------------------------
;; ---- Testing ----

;; (def data (atom nil))

;; (reset! data (Calculation/create
;;                (create-tax-calculation-params {:amount 1000
;;                                                ;;  :line1 "920 5th Ave"
;;                                                ;;  :city "Seattle"
;;                                                 ;; :state "WA"
;;                                                 :postal-code "98104"})))


;; (println @data)
;; (defn extract-calculation-amounts [calculation]
;;   {:amount-total (.getAmountTotal calculation)
;;    :tax-amount-exclusive (.getTaxAmountExclusive calculation)
;;    :tax-amount-inclusive (.getTaxAmountInclusive calculation)})
;;   ;;  :subtotal-amount (.getAmountSubtotal calculation)})

;; (extract-calculation-amounts @data)
