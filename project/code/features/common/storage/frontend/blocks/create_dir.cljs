
(ns features.common.storage.frontend.blocks.create-dir
  (:require
    [re-frame.core :as r]
    [ui.popup      :as popup]
    [ui.text-field :as text-field]
    [ui.button     :as button]))


;; -----------------------------------------------------------------------------
;; ---- Create Directory ----

(defn create-dir-callback [response props]
  (let [data (-> response :storage/create-directory!)
        path (:path props [:storage :downloaded-items])]
    (r/dispatch [:db/update-in path conj data])))

(r/reg-event-fx
  :storage/create-dir!
  (fn [{:keys [db]} [_ & [dir-id props]]]
    (let [params {:alias          (get-in db [:storage :dir-name])
                  :destination-id (or dir-id (-> (get-in db [:storage :path]) last :id))}]
      {:dispatch [:pathom/ws-request!
                  {:callback (fn [resp] (create-dir-callback resp props))
                   :query    [`(storage/create-directory! ~params)]}]})))

;; ---- Create Directory ----
;; -----------------------------------------------------------------------------

(defn modal [id props]
  [popup/view {:state    @(r/subscribe [:db/get id])
               :on-open  (fn [] (r/dispatch [:db/assoc-in [:storage :dir-name] "Test"]))
               :on-close (fn []
                           (r/dispatch [:db/dissoc id])
                           (r/dispatch [:db/dissoc-in [:storage :dir-name]]))
               :style    {:z-index 2}
               :cover    {:z-index 1}}
   
    [:div {:style {:display "grid" :gap "15px"}}
      [:div {:class "title"} "Create directory"]
      [text-field/view {:value       @(r/subscribe [:db/get-in [:storage :dir-name]])
                        :placeholder "Directory name"
                        :on-change   #(r/dispatch [:db/assoc-in [:storage :dir-name] %])
                        :style       {:width "300px"}}]

      [:div {:style {:display         "flex"
                     :gap             "8px"
                     :align-items     "center"
                     :justify-content "flex-end"}}
     
        [button/view {:mode     :clear_2
                      :type     :secondary
                      :on-click #(r/dispatch [:db/dissoc id])}
         "Cancel"]
     
        [button/view {:type :primary
                      :on-click #(do (r/dispatch [:storage/create-dir! nil props])
                                     (r/dispatch [:db/dissoc id]))
                      :disabled (empty? @(r/subscribe [:db/get-in [:storage :dir-name]]))}
          "Create"]]]])

(defn icon-button [props]
  [:<>
    [modal ::create-dir-popup props]
    [button/view {:on-click #(r/dispatch [:db/assoc ::create-dir-popup true])}
      [:i {:class ["fa-solid" "fa-folder-plus"]}]]])