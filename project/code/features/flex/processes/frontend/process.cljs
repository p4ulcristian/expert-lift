(ns features.flex.processes.frontend.process
  (:require
   [features.flex.processes.frontend.request :as processes-request]
   [features.flex.shared.frontend.components.body :as edit-page]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.text-field :as text-field]
   [ui.textarea :as textarea]
   [zero.frontend.react :as zero-react]))

(defn get-process-data [process-id]
  (if (= process-id "new")
    (do
      (r/dispatch [:db/assoc-in [:flex/process] {}])
      (r/dispatch [:db/assoc-in [:flex/process :process/name] ""])
      (r/dispatch [:db/assoc-in [:flex/process :process/description] ""]))
    (let [workspace-id @(r/subscribe [:workspace/get-id])]
      (processes-request/get-process 
       workspace-id 
       process-id
       (fn [response]
         (println "Response:" response)
         (r/dispatch [:db/assoc-in [:flex/process] response]))))))



(defn handle-submit []
  (let [process-data @(r/subscribe [:db/get-in [:flex/process]])
        wsid @(r/subscribe [:workspace/get-id])
        process-id @(r/subscribe [:db/get-in [:router :path-params :process-id]])]
    (if (= process-id "new")
      (processes-request/create-process 
       wsid 
       {:process/name (:process/name process-data) :process/description (:process/description process-data)}
       (fn [response]
         (println "Response:" response)
         (router/navigate! {:path (str "/flex/ws/" wsid "/processes")})))
      (processes-request/edit-process 
       wsid 
       {:process/id process-id :process/name (:process/name process-data) :process/description (:process/description process-data)}
       (fn [response]
         (println "Response:" response)
         (router/navigate! {:path (str "/flex/ws/" wsid "/processes")}))))))

(defn process-form []
  (let [process-id @(r/subscribe [:db/get-in [:router :path-params :process-id]])]
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
                       :value @(r/subscribe [:db/get-in [:flex/process :process/name] ""])
                       :on-change #(r/dispatch [:db/assoc-in [:flex/process :process/name] %])}]
      [textarea/view {:label "Description"
                     :value @(r/subscribe [:db/get-in [:flex/process :process/description] ""])
                     :on-change #(r/dispatch [:db/assoc-in [:flex/process :process/description] %])
                     :rows 6}]]
     [:div {:style {:text-align "center"}}
      [button/view {:mode :filled
                   :color "var(--seco-clr)"
                   :on-click handle-submit}
       (if (= process-id "new") "Add Process" "Save Changes")]]]))

(defn process []
  (let [process-id @(r/subscribe [:db/get-in [:router :path-params :process-id]])
        wsid @(r/subscribe [:workspace/get-id])]
    [edit-page/view
     {:title (if (= process-id "new") "New Process" "Edit Process")
      :description "Create, edit, and remove processes."
      :title-buttons (list
                      (when (and process-id (not= process-id "new"))
                        ^{:key "delete"}
                        [button/view {:mode :outlined
                                     :color "var(--seco-clr)"
                                     :style {:fontWeight 500 
                                            :padding "8px 20px"}
                                     :on-click #(processes-request/delete-process 
                                                wsid 
                                                process-id
                                                (fn [_]
                                                  (router/navigate! {:path (str "/flex/ws/" wsid "/processes")})))}
                         "Delete"])
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/processes")})}
                       "Back"])
      :body [process-form]}]))

(defn view []
  (let [process-id (r/subscribe [:db/get-in [:router :path-params :process-id]])]
    (zero-react/use-effect
     {:mount (fn []
               (get-process-data @process-id)
)})
    [:div
     [process]])) 