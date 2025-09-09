
(ns features.common.storage.frontend.blocks.rename-popup
  (:require
    [re-frame.core :as r]
    [ui.popup :as popup]
    [ui.text-field :as text-field]
    [ui.button :as button]))

(defn view [id props]
  (let [editing-item @(r/subscribe [:db/get-in [:storage :editing-item]])
        item-id           (:id editing-item)
        item-alias        (:alias editing-item)]
    [popup/view {:state    @(r/subscribe [:db/get id])
                 ;;  :on-open  (fn [] (r/dispatch [:db/assoc-in [:storage :rename-field] "Test"]))
                 :on-close (fn []
                             (r/dispatch [:db/dissoc id])
                             (r/dispatch [:db/dissoc-in [:storage :editing-item :alias]]))
                 :style    {:z-index 2}
                 :cover    {:z-index 1}}                       
     
      [:div {:style {:display "grid" :gap "15px"}}
        [:div {:class "title"} "Rename"]
        [text-field/view {:id          "rename-field"
                          :placeholder "Name"
                          :style       {:width "300px"}
                          :override    {:spellcheck "false"}
                          :value       item-alias
                          :on-change   #(r/dispatch [:db/assoc-in [:storage :editing-item :alias] %])
                          :on-enter    #(do (r/dispatch [:storage/rename-item! item-id item-alias])
                                            (r/dispatch [:db/dissoc id])
                                            (r/dispatch [:db/dissoc-in [:storage :editing-item]]))
                          :on-mount    #(.select (.getElementById js/document "rename-field"))}]

        [:div {:style {:display         "flex"
                       :gap             "8px"
                       :align-items     "center"
                       :justify-content "flex-end"}}
       
          [button/view {:mode     :clear_2
                        :type     :secondary
                        :on-click #(r/dispatch [:db/dissoc id])}
            "Cancel"]
       
          [button/view {:type :primary
                        :on-click #(do 
                                     (r/dispatch [:storage/rename-item! item-id item-alias])
                                     (r/dispatch [:db/dissoc id])
                                     (r/dispatch [:db/dissoc-in [:storage :editing-item]]))
                        :disabled (empty? item-alias)}
            "Save"]]]]))