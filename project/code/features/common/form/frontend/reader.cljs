
(ns features.common.form.frontend.reader
  (:require
    ["react"        :as react]
    [re-frame.core  :as r]

    [features.common.key-replacer :as key-replacer]

    [features.common.form.frontend.inputs.switch :as switch]
    [features.common.form.frontend.inputs.digit  :as digit]
    [features.common.form.frontend.inputs.select :as select]))
   
;; -----------------------------------------------------------------------------
;; ---- Utils ----

(r/reg-sub
  :form.input.condition/validate!
  (fn [db [_ condition & [form-path]]]
    ;; Replace re-frame path in the string with value from re-frame db
    (key-replacer/condition-reader condition
      (fn [value-path]
        (get-in db (concat form-path ["formdata"] value-path [:value])
                   false)))))

(r/reg-sub
  :form.dictionary/get-by-id
  (fn [db [_ id]]
    (get-in db [:forms/dictionary id])))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(defn form-reader-init-callback [response]
  (let [data (get response :form.reader/get-by-id)]
    (r/dispatch [:db/assoc-in [:forms (:id data)] data])))

(r/reg-event-fx
  :form.reader/fetch!
  (fn [{:keys [db]} [_ form-id]]
    {:pathom/request {:query    [`(:form.reader/get-by-id {:id ~form-id})]
                      :callback form-reader-init-callback}}))

;; ---- Effects ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn- group-label [{:keys [helper info-text title marked? required?]}]
  (when title
    [:h3 {:style {:font-size "16px"
                  :font-weight "bold"}}
      title]))

(defn- default-group [props & content]
  [:div 
    [group-label props]
    [:div {:style {:margin-left "15px"}}
      content]])

(defn- input-broker 
  ( [{:keys [id type value-path condition] :as input-props
      :or   {condition true}}]

    (let [display?    (if (or (empty? condition) (true? condition))
                        true
                        @(r/subscribe [:form.input.condition/validate! condition value-path]))]

      (when display?
        (case type
          "select"      ^{:key id}[select/view input-props]
          "digit_input" ^{:key id}[digit/view  input-props]
          "switch"      ^{:key id}[switch/view input-props]
          "group"       ^{:key id}[default-group input-props (map #(input-broker %) (:inputs input-props))]
          [:div [:b (str type)] " is not a valid input type."]))))
  
  ( [{:keys [id condition] :as input-props
      :or   {condition true}}
     {:as inputs
      :keys [select switch digit_input group]}
     formdata-path]
   
    (react/useEffect
      (fn []
        (r/dispatch [:db/assoc-in [:forms/dictionary id] (:title input-props)])
        (fn []))
      #js[])
   
    (let [input-props (-> input-props
                          (update :value-path #(->> % (concat formdata-path ["formdata"]) vec))
                          (assoc :formdata-path formdata-path))
          display?    (if (or (empty? condition) (true? condition))
                        true
                        @(r/subscribe [:form.input.condition/validate! condition formdata-path]))]
      (when display?
        (case (:type input-props)
          "select"          ^{:key id}(if select      [select      input-props] [select/view input-props])
          "switch"          ^{:key id}(if switch      [switch      input-props] [switch/view input-props])
          "digit_input"     ^{:key id}(if digit_input [digit_input input-props] [digit/view  input-props])
          "group"           ^{:key id}(if group
                                        [group         input-props 
                                          (map #(input-broker % inputs formdata-path) (:inputs input-props))]
                                        [default-group input-props 
                                          (map #(input-broker % inputs formdata-path) (:inputs input-props))])
          [:div [:b (str (:type input-props))] " is not a valid input type."])))))

(defn read-by-template-data-with-custom-input [inputs form-template value-path]
  [:<>
    (map (fn [input-props]
           ^{:key (-> input-props :id str)}
           [input-broker input-props inputs value-path])
         form-template)])

(defn read-by-template-data [template-data]
  [:<>
    (map (fn [input-props]
           ^{:key (-> input-props :id str)}
           [input-broker input-props]) 
        template-data)])

(defn- reader
  ;; Optionally can use form-id as first param.
  ;; The component get the form data from the db and using the template to render the form.

  ( [form-id {:keys [init-data value-path] :as config}]

    (react/useEffect
      (fn []
        (when init-data
          (r/dispatch [:db/assoc-in (conj value-path "formdata") init-data]))
        (when form-id
          (r/dispatch [:form.reader/fetch! form-id]))

        (fn []))
      #js[])
    (when-let [form-template @(r/subscribe [:db/get-in [:forms form-id :template]])]
      [reader (assoc config :form-template form-template)]))
 

  ( [{:keys [init-data form-template on-change value-path inputs clear?] :or {clear? true}}]
   ;; init-data     (hash-map)
   ;; form-template (vector of input-entities)
   ;; inputs        (hash-map)
   ;; clear?        (boolean)

   ;;Example
   ;; init-data {:my-input {:value 2 :label "asdf"}}
   ;; form-temp
    (react/useEffect
      (fn []
        (when init-data
            (r/dispatch [:db/assoc-in ["formdata"] init-data]))
       
        (fn []
          (when clear?
            (r/dispatch [:db/dissoc-in ["formdata"]]))))
      #js[])
   
    (let [current-formdata @(r/subscribe [:db/get-in (conj value-path "formdata")])]
      (react/useEffect
        (fn []
          (when on-change
            (on-change current-formdata))
          (fn []))
        #js[current-formdata]))
    (try
      [:<>
        (cond
          (not (nil? inputs)) [read-by-template-data-with-custom-input inputs form-template value-path]
          form-template       [read-by-template-data form-template]
          :else "failed to load template")]
      (catch :default e
         (println  e)))))      

 
;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view 
  ( [config]
    [reader config])
  
  ( [form-id config]
    [reader form-id config]))
  