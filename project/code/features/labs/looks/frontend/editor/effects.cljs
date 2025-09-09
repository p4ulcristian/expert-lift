
(ns features.labs.looks.frontend.editor.effects
  (:require
   [features.labs.looks.frontend.editor.controls :as controls]
   [re-frame.core :as r]
   [ui.notification :as notification]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn clean-layers [layers]
  (->> layers
       (remove #(or (empty? %) 
                    (some empty? (vals %))))
       vec))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Init Editor ----

(r/reg-event-fx :labs.looks.editor/init!
  (fn [_ _]
    {:dispatch [:db/assoc-in [:labs] {:price-group-key ""
                                      :name            ""
                                      :basecolor       ""
                                      :color-family    ""
                                      :tags            [""]
                                      :layers          [{} {} {} {}]}]}))
;; ---- Init Editor ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Layers Suggestions ----

(defn get-layers-suggestions-callback [response]
  (let [data (-> response (get :looks.layers/get-suggestions!))]
    (r/dispatch [:db/assoc-in [:labs :layers-suggestions] data])))

(r/reg-event-fx :labs.looks.layers/get-suggestions!
  (fn [_ _]
    {:dispatch [:pathom/ws-request!
                 {:callback get-layers-suggestions-callback
                  :query    `[:looks.layers/get-suggestions!]}]}))

;; ---- Get Layers Suggestions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Name Suggestions ----

(defn get-name-suggestions-callback [response update-autocomplete]
  (let [data (-> response (get :looks.name/get-suggestions!))]
    (r/dispatch [:db/assoc-in [:labs :name-suggestions] (mapv :name data)])
    (update-autocomplete (mapv :name data))))

(r/reg-event-fx :labs.looks.name/get-suggestions!
  (fn [_ [_ search update-autocomplete]]
    (let [params {:search (if (empty? search) nil search)}]
      {:dispatch [:pathom/ws-request!
                   {:callback (fn [response]
                                (get-name-suggestions-callback response update-autocomplete))
                    :query    `[(:looks.name/get-suggestions! ~params)]}]})))

;; ---- Get Name Suggestions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Look Save ----

(defn create-look-callback [response]
  (let [data (-> response (get :looks/create!))]
    (if (= data 1)
      (notification/success! "create-look" "Look created successfully")
      (notification/error!   "create-look" (:detail data)))))

(r/reg-event-fx :labs.looks/create!
  (fn [{:keys [db]} [_ texture-props]]
    (let [base-color      (get-in texture-props ["material" "color"])
          tags            (get-in db [:labs :tags])
          name            (get-in db [:labs :name])
          thumbnail       (get-in db [:labs :base64])
          price-group-key (get-in db [:labs :price-group-key])
          layers          (clean-layers (get-in db [:labs :layers]))
          layers-count    (count layers)
          color-family    (get-in db [:labs :color-family])]
      
      {:dispatch-n [[:notifications/loading! "create-look" "Creating look..."]
                    [:pathom/ws-request!
                     {:callback create-look-callback
                      :query    `[(looks/create! ~{:texture-props   texture-props
                                                   :basecolor       base-color
                                                   :color-family    color-family
                                                   :name            name
                                                   :tags            tags
                                                   :thumbnail       thumbnail
                                                   :layers          layers
                                                   :layers-count    layers-count
                                                   :price-group-key price-group-key})]}]]})))

;; ---- Look Save ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Look Update ----

(defn update-look-callback [response]
  (let [data (-> response (get :looks/update!))]
    (if (= data 1)
      (notification/success! "update-look" "Look updated successfully")
      (notification/error!   "update-look" "Failed to update look"))))

(r/reg-event-fx :labs.looks/update!
  (fn [{:keys [db]} [_ texture-props]]
    (let [base-color      (get-in texture-props ["material" "color"])
          id              (get-in db [:labs :id])
          tags            (get-in db [:labs :tags])
          name            (get-in db [:labs :name])
          thumbnail       (get-in db [:labs :base64])
          price-group-key (get-in db [:labs :price-group-key])
          layers          (clean-layers (get-in db [:labs :layers]))
          layers-count    (count layers)
          color-family    (get-in db [:labs :color-family])]
      
      {:dispatch-n [[:notifications/loading! "update-look" "Updating look..."]
                    [:pathom/ws-request!
                      {:callback update-look-callback
                       :query    `[(looks/update! ~{:id              id
                                                    :texture-props   texture-props
                                                    :basecolor       base-color
                                                    :color-family    color-family
                                                    :name            name
                                                    :tags            tags
                                                    :thumbnail       thumbnail
                                                    :layers          layers
                                                    :layers-count    layers-count
                                                    :price-group-key price-group-key})]}]]})))
;; ---- Look Update ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Look Get ----

(defn stringify [data]
  (-> data
      clj->js
      js->clj))

(defn get-look-callback [response]
  (let [data         (-> response (get :looks/get!))
        texture-data (-> data :texture stringify)]

    (reset! controls/STATE texture-data)
    (r/dispatch [:db/assoc-in [:labs] {:id              (:id data)
                                       :price-group-key (:price_group_key data "")
                                       :name            (:name data "")
                                       :basecolor       (:basecolor data "")
                                       :color-family    (:color_family data "")
                                       :tags            (:tags data [])
                                       :texture         texture-data
                                       :thumbnail       (:thumbnail data)
                                       :layers          (vec (concat (:layers data) [{} {} {} {}]))}])))

(r/reg-event-fx :labs.looks/get!
  (fn [{:keys [_db]} [_ id]]
    {:dispatch [:pathom/ws-request!
                 {:callback get-look-callback
                  :query    `[(:looks/get! ~{:id id})]}]}))

;; ---- Look Get ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Look Drafts Save ----

(defn create-look-draft-callback [response]
  (let [data (-> response (get 'looks.draft/create!))]
    (if (= data 1)
      (notification/toast! :success "Look draft created successfully")
      (notification/toast! :error (:detail data)))))

(r/reg-event-fx :labs.looks.draft/create!
  (fn [{:keys [db]} [_ texture]]
    (let [name            (get-in db [:labs :draft-name])
          price-group-key (get-in db [:labs :price-group-key])
          tags            (get-in db [:labs :tags])
          basecolor       (get-in db [:labs :basecolor])
          color-family    (get-in db [:labs :color-family])
          layers          (clean-layers (get-in db [:labs :layers]))
          layers-count    (count layers)]
      {:dispatch [:pathom/ws-request!
                   {:callback create-look-draft-callback
                    :query    `[(looks.draft/create! ~{:name            name
                                                       :price-group-key price-group-key
                                                       :tags            tags
                                                       :texture         texture
                                                       :basecolor       basecolor
                                                       :color-family    color-family
                                                       :layers          layers
                                                       :layers-count    layers-count})]}]})))

;; ---- Look Drafts Save ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Look Draft Suggestions ----

(defn get-look-drafts-suggestions-callback [response update-autocomplete]
  (let [data (-> response (get :looks.draft/get-suggestions!))]
    (r/dispatch [:db/assoc-in [:labs :draft-suggestions] data])
    (when update-autocomplete
      (update-autocomplete data))))

(r/reg-event-fx :labs.looks.draft/get-suggestions!
  (fn [_ [_ search & [update-autocomplete]]]
    (let [params {:search (if (empty? search) nil search)}]
      {:dispatch [:pathom/ws-request!
                   {:callback (fn [response]
                                (get-look-drafts-suggestions-callback response update-autocomplete))
                    :query    `[(:looks.draft/get-suggestions! ~params)]}]})))

;; ---- Get Look Draft Suggestions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- List Look Drafts ----

(defn list-look-drafts-callback [response]
  (let [data (-> response (get :looks.draft/list!))]
    (r/dispatch [:db/assoc-in [:labs :drafts] data])))

(r/reg-event-fx :labs.looks.draft/list!
  (fn [{:keys [_db]} [_ name popover-update]]
    (let [params {:search (if (empty? name) nil name)}]
      {:dispatch [:pathom/ws-request!
                  {:callback (fn [response]
                               (list-look-drafts-callback response)
                               (popover-update (mapv :name (-> response (get :looks.draft/list! [])))))
                   :query    [`(:looks.draft/list! ~params)]}]})))

;; ---- List Look Drafts ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Look Draft ----

(defn get-look-draft-callback [db response]
  (let [data         (-> response (get :looks.draft/get!))
        texture-data (-> data :texture stringify)]
    
    (reset! controls/STATE texture-data)
    (r/dispatch [:db/assoc-in [:labs] {:draft-name      (get-in db [:labs :draft-name] "")
                                       :model           (get-in db [:labs :model])
                                       :price-group-key (:price_group_key data "")
                                       :name            (:name data "")
                                       :basecolor       (:basecolor data "")
                                       :color-family    (:color_family data "")
                                       :tags            (:tags data [])
                                       :texture         texture-data
                                       :thumbnail       (:thumbnail data)
                                       :layers          (vec (concat (:layers data) [{} {} {} {}]))}])))

(r/reg-event-fx :labs.looks.draft/get!
  (fn [{:keys [db]} [_ id]]
    {:dispatch [:pathom/ws-request!
                 {:callback (fn [response]
                              (get-look-draft-callback db response))
                              
                  :query    `[(:looks.draft/get! ~{:id id})]}]}))


;; ---- Get Look Draft ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Update Look Draft ----

(defn update-color-draft-callback [response]
  (let [data (-> response (get 'looks/update-color-draft))]
    (if (instance? js/Error data)
      (notification/toast! :error "Failed to update color draft. Please try again.")
      (notification/toast! :success "Look draft updated successfully"))))

(r/reg-event-fx :labs.looks.draft/update!
  (fn [{:keys [_db]} [_ {:keys [name price-group-key tags texture basecolor]}]]
    {:dispatch [:pathom/ws-request!
                {:callback update-color-draft-callback
                 :query    `[(looks/update-color-draft ~{:name            name
                                                         :price-group-key price-group-key
                                                         :tags            tags
                                                         :texture         texture
                                                         :basecolor       basecolor})]}]}))

;; ---- Update Look Draft ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Delete Look Draft ----

(defn delete-color-draft-callback [response]
  (let [data (-> response (get 'looks/delete-color-draft))]
    (if (instance? js/Error data)
      (notification/toast! :error "Failed to delete color draft. Please try again.")
      (notification/toast! :success "Look draft deleted successfully"))))

(r/reg-event-fx :labs.looks.draft/delete!
  (fn [{:keys [_db]} [_ name]]
    {:dispatch [:pathom/ws-request!
                {:callback delete-color-draft-callback
                 :query    `[(looks/delete-color-draft ~{:name name})]}]}))

;; ---- Delete Look Draft ----
;; -----------------------------------------------------------------------------
