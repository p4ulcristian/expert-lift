
(ns features.common.form.frontend.inputs.select
  (:require
    [ui.select :as select]
    [re-frame.core :as r]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn select [{:keys [id value-path title label value options]}]
  [select/view {:title      [:b title]
                :options    options
                :value      @(r/subscribe [:db/get-in value-path])
                :on-select  (fn [value]
                              (r/dispatch [:db/assoc-in value-path value]))
                :override   {:style {:width "100%"}}}])
                
                
 
;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [config]
  [select config])