(ns ui.form
  (:require
   [re-frame.core :as r]
   [reagent.core :as reagent]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn get-form-data [form-id]
  @(r/subscribe [:db/get-in [:forms form-id :data] {}]))

(defn set-form-data! [form-id data]
  (r/dispatch [:db/assoc-in [:forms form-id :data] data]))

(defn get-form-errors [form-id]
  @(r/subscribe [:db/get-in [:forms form-id :errors] {}]))

(defn set-form-errors! [form-id errors]
  (r/dispatch [:db/assoc-in [:forms form-id :errors] errors]))

(defn validate-field [rules value]
  (let [errors (keep #(when ((:test %) value) (:msg %)) rules)]
    (when (seq errors) (first errors))))

(defn validate-form [form-id fields]
  (let [data (get-form-data form-id)
        errors (reduce (fn [acc [field-id {:keys [rules]}]]
                        (if-let [error (validate-field rules (get data field-id))]
                          (assoc acc field-id error)
                          acc))
                      {}
                      fields)]
    (set-form-errors! form-id errors)
    (empty? errors)))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn form [{:keys [id _fields _on-submit] :as _props} & _children]
  (let [form-id (or id (random-uuid))]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (set-form-data! form-id {})
        (set-form-errors! form-id {}))

      :reagent-render
      (fn [{:keys [_id fields on-submit] :as _props} & children]
        [:form {:on-submit (fn [e]
                            (.preventDefault ^js e)
                            (when (validate-form form-id fields)
                              (on-submit (get-form-data form-id))))}
         (into [:<>] children)])})))

(defn field [{:keys [form-id field-id type _rules] :as _props}]
  (let [value (get (get-form-data form-id) field-id)
        error (get (get-form-errors form-id) field-id)]
    [:div {:class "form-field"}
     (case type
       :text [:input {:type "text"
                      :value value
                      :on-change #(set-form-data! form-id
                                                 (assoc (get-form-data form-id)
                                                        field-id
                                                        (-> ^js % .-target .-value)))}]
       :number [:input {:type "number"
                        :value value
                        :on-change #(set-form-data! form-id
                                                   (assoc (get-form-data form-id)
                                                          field-id
                                                          (-> ^js % .-target .-valueAsNumber)))}]
       :select [:select {:value value
                         :on-change #(set-form-data! form-id
                                                    (assoc (get-form-data form-id)
                                                           field-id
                                                           (-> ^js % .-target .-value)))}])
     (when error
       [:div {:class "form-error"} error])]))

;; ---- Components ----
;; ----------------------------------------------------------------------------- 