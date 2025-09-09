
(ns features.labs.forms.backend.mutations
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [features.labs.forms.backend.db :as db]))

(defn save-form-data-fn [{:keys [_request] :as _env} {:keys [form-data]}]
  (println "form-data: " form-data)
  (println "form-data type: " (type form-data))
  (println "form-data keys: " (keys form-data))
  (try
    (if (:id form-data)
      (let [updated-form (db/update-form
                           {:id            (java.util.UUID/fromString (:id form-data))
                            :title         (:title form-data)
                            :template      (:template form-data)
                            :price_formula (:price_formula form-data)})]
        updated-form)
      (let [new-id (java.util.UUID/randomUUID)
            created-form (db/create-form
                           {:id            new-id
                            :title         (:title form-data "")
                            :template      (:template form-data)
                            :price_formula ""})]
        (println "Creating form with ID:" new-id)
        (println "Form title:" (:title form-data ""))
        (println "Form template:" (:template form-data))
        (println "Form price_formula:"  (:price_formula form-data))
        new-id))
    (catch Exception e
      (println "Error saving form data:" (.getMessage e))
      :error)))

(defn delete-form-fn [{:keys [_request] :as _env} {:keys [form-id]}]
  (println "Deleting form with id: " form-id)
  (try
    (db/delete-form {:id (java.util.UUID/fromString form-id)})
    {:success true}
    (catch Exception e
      (println "Error deleting form:" (.getMessage e))
      {:error (.getMessage e)})))

(pco/defmutation save-form-data! [env mutation-props]
  {::pco/op-name 'forms/save-form-data!}
  (save-form-data-fn env mutation-props))

(pco/defmutation delete-form! [env mutation-props]
  {::pco/op-name 'forms/delete-form!}
  (delete-form-fn env mutation-props))

(def mutations [save-form-data! delete-form!])