(ns features.flex.inventory.frontend.inventory
  (:require
   [features.flex.inventory.frontend.request :as inventory-request]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.table.zero :as table]
   [zero.frontend.react :as zero-react]))

;; --- Table Column Renderers ---

(defn actions-cell [wsid row]
  [button/view {:mode :outlined
                :color "var(--seco-clr)"
                :style {:padding "4px 12px" :fontSize "0.95em"}
                :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/inventory/" (:id row))})}
   "Edit"])

(defn quantity-cell [_wsid row]
  (let [quantity (:quantity row)
        min-qty (:min_qty row)
        is-low-stock (and quantity min-qty (<= quantity min-qty))
        unit (:unit row "pcs")]
    [:span {:style {:color (if is-low-stock "#dc2626" "#374151")
                   :font-weight (if is-low-stock "600" "400")}}
     (str quantity " " unit)]))

(defn min-qty-cell [_wsid row]
  (let [min-qty (:min_qty row)
        unit (:unit row "pcs")]
    [:span (str min-qty " " unit)]))

(defn category-cell [_wsid row]
  (let [category (:item_category row)]
    (if category
      [:span {:style {:background "#e5e7eb"
                     :color "#374151"
                     :padding "2px 8px"
                     :border-radius "4px"
                     :font-size "0.8em"
                     :text-transform "capitalize"}}
       category]
      [:span "—"])))

;; --- Table Row Element ---
(defn inventory-table-row-element [style content]
  [:div {:style (merge style
                       {:transition "all 0.2s ease"
                        :cursor "pointer"})}
   content])

;; --- Table Component ---
(defn inventory-table [inventory wsid]
  [table/view
   {:rows inventory
    :grid-template-columns "2fr 1fr 1fr 1fr 1fr 1fr 1fr auto"
    :columns [:name :category :type :quantity :min_qty :unit :supplier :actions]
    :labels {:name "Name"
             :category "Category"
             :type "Type"
             :quantity "Quantity"
             :min_qty "Min Qty"
             :unit "Unit"
             :supplier "Supplier"
             :actions "Actions"}
    :column-elements {:actions (fn [_item row] (actions-cell wsid row))
                     :quantity (fn [_item row] (quantity-cell wsid row))
                     :min_qty (fn [_item row] (min-qty-cell wsid row))
                     :category (fn [_item row] (category-cell wsid row))}
    :row-element inventory-table-row-element}])

;; --- Main View ---
(defn view []
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (zero-react/use-effect 
     {:mount (fn [] 
               (inventory-request/get-inventory
                workspace-id
                (fn [response]
                             (println "Response:" response)
                             (r/dispatch [:db/assoc-in [:inventory :list] response]))))})
    (let [wsid (r/subscribe [:workspace/get-id])
          inventory @(r/subscribe [:db/get-in [:inventory :list] []])]
    [body/view
     {:title "Inventory"
      :description "Manage powder coating materials and supplies inventory."
      :title-buttons (list
                      ^{:key "add"}
                      [button/view {:mode :filled
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" @wsid "/inventory/new")})}
                       "Add Item"])
      :body [inventory-table inventory @wsid]}]))) 