(ns features.flex.inventory.frontend.inventory-item
  (:require
   [features.flex.inventory.frontend.request :as inventory-request]
   [features.common.storage.frontend.picker :as picker]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.select :as select]
   [ui.text-field :as text-field]
   [ui.textarea :as textarea]
   [zero.frontend.react :as zero-react]))

(defn safe-parse-int 
  "Safely parse a string to integer, returning 0 for empty/invalid input"
  [value]
  (if (or (empty? value) (= value ""))
    0
    (let [parsed (js/parseInt value 10)]
      (if (js/isNaN parsed) 0 parsed))))

(defn get-inventory-item-data [inventory-item-id workspace-id]
  (if (= inventory-item-id "new")
    (do
      (r/dispatch [:db/assoc-in [:inventory-item] {}])
      (r/dispatch [:db/assoc-in [:inventory-item :name] ""])
      (r/dispatch [:db/assoc-in [:inventory-item :description] ""])
      (r/dispatch [:db/assoc-in [:inventory-item :category] ""])
      (r/dispatch [:db/assoc-in [:inventory-item :type] ""])
      (r/dispatch [:db/assoc-in [:inventory-item :quantity] 0])
      (r/dispatch [:db/assoc-in [:inventory-item :min_qty] 0])
      (r/dispatch [:db/assoc-in [:inventory-item :unit] "pcs"])
      (r/dispatch [:db/assoc-in [:inventory-item :supplier] ""])
      (r/dispatch [:db/assoc-in [:inventory-item :item_category] "material"])
      (r/dispatch [:db/assoc-in [:inventory-item :picture_url] ""]))
    (inventory-request/get-inventory-item
     workspace-id inventory-item-id
     (fn [response]
       (println "Response:" response)
       (r/dispatch [:db/assoc-in [:inventory-item] response])))))

(defn picture-section []
  [:div
   [:label {:class "input--text-field--label"} "Picture"]
   [:div {:style {:width "200px"
                 :height "150px"
                 :border-radius "8px"
                 :overflow "hidden"
                 :border "1px solid #ddd"
                 :margin-bottom "8px"
                 :background-color "#f9f9f9"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"}}
    (let [picture-url @(r/subscribe [:db/get-in [:inventory-item :picture_url] ""])]
      (if (not-empty picture-url)
        [:img {:src picture-url
               :style {:width "100%"
                      :height "100%"
                      :object-fit "cover"}}]
        [:i {:class "fa-solid fa-image"
             :style {:font-size "32px"
                    :color "#ccc"}}]))]
   [picker/view
    {:value @(r/subscribe [:db/get-in [:inventory-item :picture_url] ""])
     :multiple? false
     :on-select #(r/dispatch [:db/assoc-in [:inventory-item :picture_url] (:url %)])
     :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]])

(defn name-field []
  [text-field/view {:label "Name"
                   :value @(r/subscribe [:db/get-in [:inventory-item :name] ""])
                   :on-change #(r/dispatch [:db/assoc-in [:inventory-item :name] %])}])

(defn category-field []
  [:div {:style {:width "100%"}}
   [:label {:class "input--text-field--label"} "Category"]
   (let [category-options [{:value "electronics" :label "Electronics"}
                          {:value "office-supplies" :label "Office Supplies"}
                          {:value "raw-materials" :label "Raw Materials"}
                          {:value "finished-goods" :label "Finished Goods"}
                          {:value "maintenance" :label "Maintenance & Repair"}
                          {:value "safety" :label "Safety Equipment"}
                          {:value "packaging" :label "Packaging"}
                          {:value "cleaning" :label "Cleaning Supplies"}
                          {:value "hardware" :label "Hardware"}
                          {:value "laboratory" :label "Laboratory"}
                          {:value "other" :label "Other"}]
         current-value @(r/subscribe [:db/get-in [:inventory-item :category] ""])
         selected-option (first (filter #(= (:value %) current-value) category-options))]
     [select/view {:value selected-option
                  :override {:style {:height "40px" :width "100%"}}
                  :options category-options
                  :on-select #(r/dispatch [:db/assoc-in [:inventory-item :category] (:value %)])}])])

(defn type-field []
  [text-field/view {:label "Type"
                   :value @(r/subscribe [:db/get-in [:inventory-item :type] ""])
                   :on-change #(r/dispatch [:db/assoc-in [:inventory-item :type] %])}])

(defn item-category-field []
  [:div {:style {:width "100%"}}
   [:label {:class "input--text-field--label"} "Item Category"]
   (let [item-category-options [{:value "material" :label "Material"}
                               {:value "component" :label "Component"}
                               {:value "equipment" :label "Equipment"}
                               {:value "consumable" :label "Consumable"}
                               {:value "tool" :label "Tool"}
                               {:value "spare" :label "Spare"}
                               {:value "chemical" :label "Chemical"}]
         current-value @(r/subscribe [:db/get-in [:inventory-item :item_category] "material"])
         selected-option (first (filter #(= (:value %) current-value) item-category-options))]
     [select/view {:value selected-option
                  :override {:style {:height "40px" :width "100%"}}
                  :options item-category-options
                  :on-select #(r/dispatch [:db/assoc-in [:inventory-item :item_category] (:value %)])}])])

(defn quantity-field []
  [text-field/view {:label "Quantity"
                   :type "number"
                   :value (str @(r/subscribe [:db/get-in [:inventory-item :quantity] 0]))
                   :on-change #(r/dispatch [:db/assoc-in [:inventory-item :quantity] (safe-parse-int %)])}])

(defn min-quantity-field []
  [text-field/view {:label "Minimum Quantity"
                   :type "number"
                   :value (str @(r/subscribe [:db/get-in [:inventory-item :min_qty] 0]))
                   :on-change #(r/dispatch [:db/assoc-in [:inventory-item :min_qty] (safe-parse-int %)])}])

(defn unit-field []
  [text-field/view {:label "Unit"
                   :value @(r/subscribe [:db/get-in [:inventory-item :unit] "pcs"])
                   :on-change #(r/dispatch [:db/assoc-in [:inventory-item :unit] %])}])

(defn supplier-field []
  [text-field/view {:label "Supplier"
                   :value @(r/subscribe [:db/get-in [:inventory-item :supplier] ""])
                   :on-change #(r/dispatch [:db/assoc-in [:inventory-item :supplier] %])}])

(defn description-field []
  [textarea/view {:label "Description"
                 :value @(r/subscribe [:db/get-in [:inventory-item :description] ""])
                 :on-change #(r/dispatch [:db/assoc-in [:inventory-item :description] %])
                 :rows 4}])

(defn handle-submit []
  (let [inventory-item-data @(r/subscribe [:db/get-in [:inventory-item]])
        wsid @(r/subscribe [:workspace/get-id])
        inventory-item-id @(r/subscribe [:db/get-in [:router :path-params :inventory-item-id]])
        callback-fn (fn [response]
                      (println "Response:" response)
                      (router/navigate! {:path (str "/flex/ws/" wsid "/inventory")}))]
    (if (= inventory-item-id "new")
      (inventory-request/create-inventory-item
       wsid
       {:name (:name inventory-item-data) 
        :description (:description inventory-item-data)
        :category (:category inventory-item-data)
        :type (:type inventory-item-data)
        :quantity (:quantity inventory-item-data)
        :min_qty (:min_qty inventory-item-data)
        :unit (:unit inventory-item-data)
        :supplier (:supplier inventory-item-data)
        :item_category (:item_category inventory-item-data)
        :picture_url (:picture_url inventory-item-data)}
       callback-fn)
      (inventory-request/edit-inventory-item
       wsid
       {:id inventory-item-id 
        :name (:name inventory-item-data) 
        :description (:description inventory-item-data)
        :category (:category inventory-item-data)
        :type (:type inventory-item-data)
        :quantity (:quantity inventory-item-data)
        :min_qty (:min_qty inventory-item-data)
        :unit (:unit inventory-item-data)
        :supplier (:supplier inventory-item-data)
        :item_category (:item_category inventory-item-data)
        :picture_url (:picture_url inventory-item-data)}
       callback-fn))))

(defn inventory-item-form []
  [:div {:style {:max-width "600px"
                 :margin "0 auto"
                 :padding-top "32px"
                 :display "flex"
                 :flex-direction "column"
                 :gap "32px"}}
   ;; Top section with picture and name/category
   [:div {:style {:display "grid" 
                 :grid-template-columns "200px 1fr" 
                 :gap "32px"}}
    [picture-section]
    [:div {:style {:display "flex"
                  :flex-direction "column"
                  :gap "24px"}}
     [name-field]
     [category-field]]]
   ;; Type and Item Category section
   [:div {:style {:display "grid" 
                 :grid-template-columns "1fr 1fr" 
                 :gap "24px"}}
    [type-field]
    [item-category-field]]
   ;; Quantity section
   [:div {:style {:display "grid" 
                 :grid-template-columns "1fr 1fr 1fr" 
                 :gap "24px"}}
    [quantity-field]
    [min-quantity-field]
    [unit-field]]
   ;; Supplier section
   [:div {:style {:display "grid" 
                 :grid-template-columns "1fr" 
                 :gap "24px"}}
    [supplier-field]]
   ;; Description section
   [description-field]
   [:div {:style {:text-align "center"}}
    [button/view {:mode :filled
                  :color "var(--seco-clr)"
                  :on-click handle-submit}
     (let [inventory-item-id @(r/subscribe [:db/get-in [:router :path-params :inventory-item-id]])]
       (if (= inventory-item-id "new") "Add New" "Save Changes"))]]])


(defn inventory-item []
  (let [inventory-item-id @(r/subscribe [:db/get-in [:router :path-params :inventory-item-id]])
        wsid @(r/subscribe [:workspace/get-id])]
    [body/view
     {:title (if (= inventory-item-id "new") "New Inventory Item" "Edit Inventory Item")
      :description "Create, edit, and remove inventory items."
      :title-buttons (list
                      (when (and inventory-item-id (not= inventory-item-id "new"))
                        ^{:key "delete"}
                        [button/view {:mode :outlined
                                     :color "var(--seco-clr)"
                                     :style {:fontWeight 500 
                                            :padding "8px 20px"}
                                     :on-click #(when (js/confirm "Are you sure you want to delete this inventory item?")
                                                 (inventory-request/delete-inventory-item
                                                  wsid inventory-item-id
                                                  (fn [_]
                                                    (router/navigate! {:path (str "/flex/ws/" wsid "/inventory")}))))}
                         "Delete"])
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/inventory")})}
                       "Back"])
      :body [inventory-item-form]}]))

(defn view []
  (let [inventory-item-id (r/subscribe [:db/get-in [:router :path-params :inventory-item-id]])
        workspace-id (r/subscribe [:workspace/get-id])]
    (zero-react/use-effect
     {:mount (fn []
               (get-inventory-item-data @inventory-item-id @workspace-id))})
    [:div
     [inventory-item]]))