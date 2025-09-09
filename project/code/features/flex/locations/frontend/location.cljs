(ns features.flex.locations.frontend.location
  (:require
   [features.flex.locations.frontend.request :as locations-request]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.text-field :as text-field]
   [ui.textarea :as textarea]
   [zero.frontend.react :as zero-react]))

(defn get-location-data [location-id]
  (if (= location-id "new")
    (do
      (r/dispatch [:db/assoc-in [:location] {}])
      (r/dispatch [:db/assoc-in [:location :name] ""])
      (r/dispatch [:db/assoc-in [:location :description] ""]))
    (let [workspace-id @(r/subscribe [:workspace/get-id])]
      (locations-request/get-location 
       workspace-id 
       location-id
       (fn [response]
         (println "Response:" response)
         (r/dispatch [:db/assoc-in [:location] response]))))))

(defn location-header []
  (let [wsid @(r/subscribe [:workspace/get-id])
        location-id @(r/subscribe [:db/get-in [:router :path-params :location-id]])]
    [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :align-items "center"
                   :margin-bottom "32px"}}
     [:h1 {:style {:font-size "2rem" 
                   :font-weight 700 
                   :color "#222" 
                   :margin 0}} 
      (if (= location-id "new") "New Location" "Edit Location")]
     [:div {:style {:display "flex" :gap "12px"}}
      (when (not= location-id "new")
        [button/view {:mode :outlined
                     :color "var(--error-clr)"
                     :style {:fontWeight 500 
                            :padding "8px 20px"}
                     :on-click #(when (js/confirm "Are you sure you want to delete this location?")
                                 (locations-request/delete-location 
                                  wsid 
                                  location-id
                                  (fn [_]
                                    (router/navigate! {:path (str "/flex/ws/" wsid "/locations")}))))}
         "Delete"])
      [button/view {:mode :outlined
                   :color "var(--seco-clr)"
                   :style {:fontWeight 500 
                          :padding "8px 20px"}
                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/locations")})}
       "Back to Locations"]]]))

(defn handle-submit []
  (let [location-data @(r/subscribe [:db/get-in [:location]])
        wsid @(r/subscribe [:workspace/get-id])
        location-id @(r/subscribe [:db/get-in [:router :path-params :location-id]])]
    (if (= location-id "new")
      (locations-request/create-location 
       wsid 
       {:name (:name location-data) :description (:description location-data)}
       (fn [response]
         (println "Response:" response)
         (router/navigate! {:path (str "/flex/ws/" wsid "/locations")})))
      (locations-request/edit-location 
       wsid 
       {:id location-id :name (:name location-data) :description (:description location-data)}
       (fn [response]
         (println "Response:" response)
         (router/navigate! {:path (str "/flex/ws/" wsid "/locations")}))))))

(defn location-form []
  [:div {:style {:max-width "600px"
                 :margin "0 auto"
                 :padding-top "32px"
                 :display "flex"
                 :flex-direction "column"
                 :gap "32px"}}
   [:div {:style {:display "grid" 
                 :grid-template-columns "1fr" 
                 :gap "24px"}}
    [text-field/view {:label "Name"
                     :value @(r/subscribe [:db/get-in [:location :name] ""])
                     :on-change #(r/dispatch [:db/assoc-in [:location :name] %])}]
    [textarea/view {:label "Description"
                   :value @(r/subscribe [:db/get-in [:location :description] ""])
                   :on-change #(r/dispatch [:db/assoc-in [:location :description] %])
                   :rows 6}]]
   [:div {:style {:text-align "center"}}
    [button/view {:mode :filled
                  :color "var(--seco-clr)"
                  :on-click handle-submit}
     (let [location-id @(r/subscribe [:db/get-in [:router :path-params :location-id]])]
       (if (= location-id "new") "Add Location" "Save Changes"))]]])

(defn location []
  (let [location-id @(r/subscribe [:db/get-in [:router :path-params :location-id]])
        wsid @(r/subscribe [:workspace/get-id])]
    [body/view
     {:title (if (= location-id "new") "New Location" "Edit Location")
      :description "Create, edit, and remove locations."
      :title-buttons (list
                      (when (and location-id (not= location-id "new"))
                        ^{:key "delete"}
                        [button/view {:mode :outlined
                                     :color "var(--seco-clr)"
                                     :style {:fontWeight 500 
                                            :padding "8px 20px"}
                                     :on-click #(when (js/confirm "Are you sure you want to delete this location?")
                                                 (locations-request/delete-location 
                                                  wsid 
                                                  location-id
                                                  (fn [_]
                                                    (router/navigate! {:path (str "/flex/ws/" wsid "/locations")}))))}
                         "Delete"])
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/locations")})}
                       "Back"])
      :body [location-form]}]))

(defn view []
  (let [location-id (r/subscribe [:db/get-in [:router :path-params :location-id]])]
    (zero-react/use-effect
     {:mount (fn []
               (get-location-data @location-id))})
    [:div
     [location]]))