(ns features.labs.looks.frontend.lister.views
  (:require
   ["react"       :as react]
   [features.labs.looks.frontend.lister.effects]
   [features.labs.shared.frontend.components.header :as header]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.link :as link]
   [ui.table :as table]
   [ui.text-field :as text-field]))
;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn header []
  [:div {:class "looks-list-header"}
    [text-field/view
      {:id            "looks-search"
       :value         @(r/subscribe [:db/get-in [:labs :lister :search]])
       :on-change     #(r/dispatch [:db/assoc-in [:labs :lister :search] %])
       :on-type-ended #(r/dispatch [:labs.looks/list! {:search %}])
       :placeholder   "Search looks..."
       :override      {:auto-focus true}
       :style         {:background-color  "var(--ir-secondary)"}}]])

(defn look-column [{:keys [label]} _table-props]
  [:div {:style {:padding    "8px 0px"
                 :text-align "left"
                 :border-color "yellow"}}
   label])

(defn context-menu [{:keys [id name]}]
  [:div {:style {:background    "white"
                 :border        "1px solid"
                 :border-radius "6px"
                 :padding       "15px"
                 :display       "grid"
                 :gap           "15px"}}
    [:b name]
    [link/view {:href  (str "/irunrainbow/looks/editor/" id)
                :style {:display     "flex"
                        :align-items "center"
                        :gap         "10px"}}
      [:i {:class ["fa-solid" "fa-pen"]}]
      "Edit"]
    [button/view {:type :warning
                  :on-click #(do (r/dispatch [:labs.looks.lister/delete! id])
                                 (r/dispatch [:popover/close :row-context-menu]))
                  :style    {:display     "flex"
                             :align-items "center"
                             :gap         "10px"}}
      [:i {:class ["fa-solid" "fa-trash"]}]
      "Delete"]])

(defn open-context-menu [event row-props]
  (.preventDefault ^js event) 
  (r/dispatch [:popover/open :row-context-menu
               {:content [context-menu row-props]
                :style   {:top  (.-clientY event)
                          :left (.-clientX event)}}]))

(defn look-row [_index {:keys [id thumbnail name color_family] :as row-props} _table-props]
  [:div {:color "var(--ir-secondary)"
         :style {"--bg-clr" "var(--ir-secondary)"
                 :align-items "center"}
        ;;  :href (str "/irunrainbow/looks/editor/" id)
         :on-double-click #(router/navigate! {:path (str "/irunrainbow/looks/editor/" id)})
         :on-context-menu #(open-context-menu % row-props)}
   
    [:img {:src thumbnail :style {:width "44px" :height "44px"} :loading "lazy"}]
    [:div (or name "-")]
    [:div (or color_family "-")]])

(defn body [looks]
  [table/view
    {:data    looks
     :layout  "100px 1fr 1fr"
     :columns [nil
               {:id "name" :label "Name"}
               {:id "color-family" :label "Color Family"}]
     :style   {:background-color "var(--ir-secondary)"
               :border-color     "yellow"}}
    look-column
    look-row])

(defn looks-list []
  (let [looks @(r/subscribe [:db/get-in [:labs :lister :looks]])]
    [:div {:class "looks-list-container"}
      [header]
      [body looks]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view []
  (react/useEffect
    (fn []
      (r/dispatch [:labs.looks/list!])
      (fn []))
    #js[])

  [:<>
    [header/view]
    [looks-list]])
