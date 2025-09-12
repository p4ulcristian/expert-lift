(ns ui.form-field
  (:require [reagent.core :as r]
            [clojure.string :as str]))

(defn- field-label
  "Form field label with optional required indicator"
  [label field-key has-error? required?]
  [:label {:for (name field-key)
           :style {:display "block" :font-weight "600" :margin-bottom "0.5rem"
                   :color (if has-error? "#dc3545" "#374151")
                   :font-size "0.875rem" :line-height "1.25rem"}}
   label
   (when required?
     [:span {:style {:color "#dc3545" :margin-left "0.25rem"}} "*"])])

(defn- input-base-style [has-error?]
  "Base styling for input elements"
  {:width "100%"
   :padding "0.75rem 1rem"
   :border (if has-error? "2px solid #dc3545" "1px solid #d1d5db")
   :border-radius "8px"
   :font-size "1rem"
   :line-height "1.5"
   :transition "border-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out"
   :box-shadow (if has-error? 
                 "0 0 0 3px rgba(220, 53, 69, 0.1)" 
                 "0 1px 2px 0 rgba(0, 0, 0, 0.05)")
   :outline "none"
   :focus {:border-color (if has-error? "#dc3545" "#3b82f6")
           :box-shadow (if has-error? 
                         "0 0 0 3px rgba(220, 53, 69, 0.1)"
                         "0 0 0 3px rgba(59, 130, 246, 0.1)")}})

(defn- field-input [field-key value on-change has-error? attrs]
  "Render appropriate input type (text, textarea, etc.)"
  (let [base-props {:value (str (or value ""))
                    :on-change on-change
                    :style (merge (input-base-style has-error?) (:style attrs))}]
    (if (= (:type attrs) "textarea")
      [:textarea (merge (dissoc attrs :type) base-props)]
      [:input (merge attrs base-props)])))

(defn- field-error [error-msg]
  "Display field error message"
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn form-field
  "Complete form field with label, input, and error handling"
  [{:keys [label field-key value on-change error required? attrs]
    :or {attrs {}}}]
  (let [has-error? (not (str/blank? error))]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error? required?]
     [field-input field-key value on-change has-error? attrs]
     [field-error error]]))

(defn checkbox-field
  "Styled checkbox field with label"
  [{:keys [label checked? on-change disabled? description]}]
  [:div {:style {:margin-bottom "1.5rem" :padding "1rem" :background "#f9fafb" 
                 :border "1px solid #e5e7eb" :border-radius "8px"}}
   [:label {:style {:display "flex" :align-items "center" :font-weight "600" 
                    :color "#374151" :cursor (if disabled? "not-allowed" "pointer")}}
    [:input {:type "checkbox"
             :checked (boolean checked?)
             :disabled disabled?
             :on-change on-change
             :style {:margin-right "0.75rem" :width "1rem" :height "1rem" 
                     :accent-color "#3b82f6" :cursor (if disabled? "not-allowed" "pointer")}}]
    label
    (when description
      [:span {:style {:color "#6b7280" :font-weight "normal" :margin-left "0.5rem"}}
       (str "(" description ")")])]])