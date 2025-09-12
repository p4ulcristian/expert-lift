(ns ui.data-table
  (:require [zero.frontend.re-frame :as re-frame]
            [zero.frontend.react :as zero-react]
            [clojure.string :as str]))

;; -----------------------------------------------------------------------------
;; ---- Re-frame Events & Subscriptions ----

(re-frame/reg-event-db
  :data-table/set-search
  (fn [db [table-id search-term]]
    (-> db
        (assoc-in [:data-table table-id :search] search-term)
        (assoc-in [:data-table table-id :pagination :current-page] 0))))

(re-frame/reg-event-db
  :data-table/set-sort
  (fn [db [table-id sort-key sort-direction]]
    (-> db
        (assoc-in [:data-table table-id :sort] {:key sort-key :direction sort-direction})
        (assoc-in [:data-table table-id :pagination :current-page] 0))))

(re-frame/reg-event-db
  :data-table/set-page
  (fn [db [table-id page]]
    (assoc-in db [:data-table table-id :pagination :current-page] page)))

(re-frame/reg-event-db
  :data-table/set-page-size
  (fn [db [table-id page-size]]
    (assoc-in db [:data-table table-id :pagination :page-size] page-size)))

(re-frame/reg-event-db
  :data-table/reset-state
  (fn [db [table-id]]
    (assoc-in db [:data-table table-id] 
              {:search ""
               :sort {:key nil :direction :asc}
               :pagination {:current-page 0 :page-size 10}})))

(re-frame/reg-sub
  :data-table/get-state
  (fn [db [table-id]]
    (get-in db [:data-table table-id] 
            {:search ""
             :sort {:key nil :direction :asc}
             :pagination {:current-page 0 :page-size 10}})))

(re-frame/reg-sub
  :data-table/get-search
  (fn [db [table-id]]
    (get-in db [:data-table table-id :search] "")))

(re-frame/reg-sub
  :data-table/get-sort
  (fn [db [table-id]]
    (get-in db [:data-table table-id :sort] {:key nil :direction :asc})))

(re-frame/reg-sub
  :data-table/get-pagination
  (fn [db [table-id]]
    (get-in db [:data-table table-id :pagination] {:current-page 0 :page-size 10})))

;; ---- Re-frame Events & Subscriptions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Utility Functions ----

(defn- search-matches?
  "Check if a row matches the search term"
  [row search-term headers]
  (if (str/blank? search-term)
    true
    (let [search-lower (str/lower-case search-term)]
      (some (fn [header]
              (let [value (get row (:key header))
                    value-str (str/lower-case (str (or value "")))]
                (str/includes? value-str search-lower)))
            headers))))

(defn- sort-rows
  "Sort rows by the given sort configuration"
  [rows sort-config]
  (if (:key sort-config)
    (let [sort-fn (if (= (:direction sort-config) :desc) #(compare %2 %1) compare)]
      (sort-by #(get % (:key sort-config)) sort-fn rows))
    rows))

(defn- paginate-rows
  "Apply pagination to rows"
  [rows pagination]
  (let [page-size (:page-size pagination)
        current-page (:current-page pagination)
        start-idx (* current-page page-size)
        end-idx (+ start-idx page-size)]
    (subvec (vec rows) start-idx (min end-idx (count rows)))))

(defn- filter-sort-paginate
  "Apply search, sort, and pagination to rows"
  [rows headers table-state]
  (let [search-term (:search table-state)
        sort-config (:sort table-state)
        pagination (:pagination table-state)
        
        filtered-rows (filter #(search-matches? % search-term headers) rows)
        sorted-rows (sort-rows filtered-rows sort-config)
        total-count (count sorted-rows)
        paginated-rows (paginate-rows sorted-rows pagination)]
    
    {:rows paginated-rows
     :total-count total-count
     :filtered-count (count filtered-rows)}))

;; ---- Utility Functions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Styling Functions ----

(defn- table-header-style
  "Table header cell styling"
  []
  {:padding "1rem 1.25rem" :text-align "left" :font-weight "600" 
   :font-size "0.75rem" :letter-spacing "0.05em" :text-transform "uppercase"
   :color "#374151" :background "#f9fafb" :border-bottom "1px solid #e5e7eb"})

(defn- table-cell-style
  "Table body cell styling"
  []
  {:padding "1rem 1.25rem" :border-bottom "1px solid #f3f4f6" :vertical-align "top"})

(defn- action-button
  "Styled action button for table rows"
  [text on-click variant]
  (let [variant-styles (case variant
                         :primary {:background "#f3f4f6" :color "#374151" 
                                   :border "1px solid #d1d5db"
                                   :hover {:background "#e5e7eb" :border-color "#9ca3af"}}
                         :danger {:background "#fef2f2" :color "#dc2626"
                                  :border "1px solid #fecaca"
                                  :hover {:background "#fee2e2" :border-color "#fca5a5"}}
                         {:background "#f3f4f6" :color "#374151" 
                          :border "1px solid #d1d5db"
                          :hover {:background "#e5e7eb" :border-color "#9ca3af"}})]
    [:button {:type "button"
              :on-click on-click
              :style (merge {:padding "0.5rem 0.75rem" :border-radius "6px" :cursor "pointer"
                            :font-size "0.75rem" :font-weight "500" :transition "all 0.15s ease-in-out"}
                           variant-styles)}
     text]))

;; ---- Styling Functions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- UI Components ----

(defn- search-input
  "Search input component"
  [table-id search-term]
  [:div {:style {:margin-bottom "1rem"}}
   [:input {:type "text"
            :placeholder "Search..."
            :value search-term
            :on-change #(re-frame/dispatch [:data-table/set-search table-id (.. % -target -value)])
            :style {:width "300px"
                    :padding "0.75rem 1rem"
                    :border "1px solid #d1d5db"
                    :border-radius "8px"
                    :font-size "0.875rem"
                    :background "white"
                    :box-shadow "0 1px 2px 0 rgba(0, 0, 0, 0.05)"
                    :outline "none"
                    :focus {:border-color "#3b82f6"
                           :box-shadow "0 0 0 3px rgba(59, 130, 246, 0.1)"}}}]])

(defn- sortable-header
  "Sortable column header"
  [header table-id sort-config]
  (let [is-sorted? (= (:key sort-config) (:key header))
        is-sortable? (:sortable? header true)
        sort-direction (:direction sort-config)
        next-direction (if (and is-sorted? (= sort-direction :asc)) :desc :asc)]
    [:th {:style (merge (table-header-style) 
                       (:style header)
                       (when is-sortable? {:cursor "pointer" :user-select "none"}))
          :on-click (when is-sortable? 
                     #(re-frame/dispatch [:data-table/set-sort table-id (:key header) next-direction]))}
     [:div {:style {:display "flex" :align-items "center" :gap "0.5rem"}}
      (:label header)
      (when is-sortable?
        [:span {:style {:color (if is-sorted? "#3b82f6" "#9ca3af")
                        :font-size "0.75rem"}}
         (cond
           (and is-sorted? (= sort-direction :asc)) "↑"
           (and is-sorted? (= sort-direction :desc)) "↓"
           :else "↕")])]]))

(defn- pagination-controls
  "Pagination controls component"
  [table-id pagination total-count]
  (let [current-page (:current-page pagination)
        page-size (:page-size pagination)
        total-pages (Math/ceil (/ total-count page-size))
        has-prev? (> current-page 0)
        has-next? (< current-page (dec total-pages))
        start-item (inc (* current-page page-size))
        end-item (min (* (inc current-page) page-size) total-count)]
    
    (when (> total-pages 1)
      [:div {:style {:display "flex" :justify-content "space-between" :align-items "center" 
                     :margin-top "1rem" :padding "1rem" :background "#f9fafb" 
                     :border-radius "8px" :border "1px solid #e5e7eb"}}
       
       ;; Info text
       [:div {:style {:color "#6b7280" :font-size "0.875rem"}}
        (str "Showing " start-item " to " end-item " of " total-count " entries")]
       
       ;; Navigation buttons
       [:div {:style {:display "flex" :gap "0.5rem"}}
        ;; Previous button
        [:button {:type "button"
                  :disabled (not has-prev?)
                  :on-click #(re-frame/dispatch [:data-table/set-page table-id (dec current-page)])
                  :style {:padding "0.5rem 0.75rem"
                          :background (if has-prev? "white" "#f3f4f6")
                          :color (if has-prev? "#374151" "#9ca3af")
                          :border "1px solid #d1d5db"
                          :border-radius "6px"
                          :cursor (if has-prev? "pointer" "not-allowed")
                          :font-size "0.875rem"}}
         "Previous"]
        
        ;; Page numbers (show current and nearby pages)
        (let [start-page (max 0 (- current-page 2))
              end-page (min total-pages (+ current-page 3))]
          (for [page (range start-page end-page)]
            ^{:key page}
            [:button {:type "button"
                      :on-click #(re-frame/dispatch [:data-table/set-page table-id page])
                      :style {:padding "0.5rem 0.75rem"
                              :background (if (= page current-page) "#3b82f6" "white")
                              :color (if (= page current-page) "white" "#374151")
                              :border "1px solid #d1d5db"
                              :border-radius "6px"
                              :cursor "pointer"
                              :font-size "0.875rem"
                              :margin "0 0.125rem"}}
             (inc page)]))
        
        ;; Next button
        [:button {:type "button"
                  :disabled (not has-next?)
                  :on-click #(re-frame/dispatch [:data-table/set-page table-id (inc current-page)])
                  :style {:padding "0.5rem 0.75rem"
                          :background (if has-next? "white" "#f3f4f6")
                          :color (if has-next? "#374151" "#9ca3af")
                          :border "1px solid #d1d5db"
                          :border-radius "6px"
                          :cursor (if has-next? "pointer" "not-allowed")
                          :font-size "0.875rem"}}
         "Next"]]])))

;; ---- UI Components ----
;; -----------------------------------------------------------------------------

(defn status-badge
  "Status badge component for displaying active/inactive states"
  [active? & {:keys [active-text inactive-text]}]
  [:span {:style {:padding "0.25rem 0.75rem" :border-radius "9999px" :font-size "0.75rem" :font-weight "500"
                  :background (if active? "#dcfce7" "#fee2e2")
                  :color (if active? "#166534" "#dc2626")}}
   (if active? 
     (or active-text "Active") 
     (or inactive-text "Inactive"))])

(defn data-table
  "Modern styled data table with client-side search, sorting, and pagination"
  [{:keys [headers rows loading? empty-message actions id-key table-id show-search? show-pagination?]
    :or {id-key :id 
         table-id :default
         show-search? true
         show-pagination? true}}]
  (let [table-state @(re-frame/subscribe [:data-table/get-state table-id])
        search-term (:search table-state)
        sort-config (:sort table-state)
        pagination (:pagination table-state)
        
        processed-data (when rows (filter-sort-paginate rows headers table-state))
        display-rows (:rows processed-data)
        total-count (:total-count processed-data)]

    [:div
      ;; Search input
      (when show-search?
        [search-input table-id search-term])
      
      (cond
        loading?
        [:div {:style {:display "flex" :justify-content "center" :align-items "center" 
                       :padding "4rem" :background "white" :border-radius "12px"
                       :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
         [:div {:style {:text-align "center"}}
          [:div {:style {:width "40px" :height "40px" :border "4px solid #f3f4f6" 
                         :border-top "4px solid #3b82f6" :border-radius "50%"
                         :animation "spin 1s linear infinite" :margin "0 auto 1rem"}}]
          [:div {:style {:color "#6b7280" :font-weight "500"}} "Loading..."]]]

        (or (empty? rows) (and total-count (= total-count 0)))
        [:div {:style {:text-align "center" :padding "4rem" :background "white" 
                       :border-radius "12px" :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
         [:div {:style {:color "#9ca3af" :font-size "1.125rem" :font-weight "500"}}
          (or empty-message "No data found")]
         [:div {:style {:color "#6b7280" :font-size "0.875rem" :margin-top "0.5rem"}}
          "There are no items to display"]]

        :else
        [:div
         ;; Table
         [:div {:style {:background "white" :border-radius "12px" :overflow "hidden"
                        :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)"
                        :border "1px solid #e5e7eb"}}
          [:table {:style {:width "100%" :border-collapse "collapse"}}
           [:thead
            [:tr
             (for [header headers]
               ^{:key (:key header)}
               [sortable-header header table-id sort-config])
             (when actions
               [:th {:style (merge (table-header-style) {:text-align "center"})} "Actions"])]]
           [:tbody
            (for [row display-rows]
              ^{:key (get row id-key)}
              [:tr {:style {:transition "background-color 0.15s ease-in-out"
                            :hover {:background "#f9fafb"}}}
               (for [header headers]
                 ^{:key (str (get row id-key) "-" (:key header))}
                 [:td {:style (merge (table-cell-style) (:cell-style header))}
                  (if (:render header)
                    ((:render header) (get row (:key header)) row)
                    (str (get row (:key header))))])
               (when actions
                 [:td {:style (merge (table-cell-style) {:text-align "center"})}
                  [:div {:style {:display "flex" :gap "0.5rem" :justify-content "center"}}
                   (for [action actions]
                     ^{:key (:key action)}
                     [action-button (:label action) 
                      #((:on-click action) row)
                      (:variant action)])]])])]]]
         
         ;; Pagination
         (when (and show-pagination? total-count)
           [pagination-controls table-id pagination total-count])])])) 

(defn server-side-data-table
  "Modern data table with server-side filtering, sorting, and pagination"
  [{:keys [headers loading? empty-message actions id-key table-id show-search? show-pagination?
           data-source data-key query-fn on-data-change]
    :or {id-key :id 
         table-id :default
         show-search? true
         show-pagination? true}}]
  (let [table-state @(re-frame/subscribe [:data-table/get-state table-id])
        search-term (:search table-state)
        sort-config (:sort table-state)
        pagination (:pagination table-state)
        
        rows (if data-key 
               (get data-source data-key [])
               (or (:worksheets data-source) 
                   (:addresses data-source)
                   (:teams data-source)
                   []))
        server-pagination (:pagination data-source)
        total-count (:total-count server-pagination 0)]
    
    ;; Normalize values to prevent unnecessary re-renders
    (let [normalized-search (or search-term "")
          normalized-sort-key (:key sort-config)
          normalized-sort-direction (name (:direction sort-config :asc))
          normalized-page (:current-page pagination 0)
          normalized-page-size (:page-size pagination 10)
          normalized-search-map {:search normalized-search
                                 :sort-by normalized-sort-key
                                 :sort-direction normalized-sort-direction
                                 :page normalized-page
                                 :page-size normalized-page-size}]
    
      ;; Use React effect to trigger data fetch when table state changes (prevents infinite loop)
      (zero-react/use-effect
       {:mount (fn []
                 (when query-fn
                   (query-fn normalized-search-map)))
        :params #js []})
      (zero-react/use-effect
        {:mount (fn []
                  (when query-fn
                    (query-fn normalized-search-map)))
         :params #js [normalized-search normalized-sort-key normalized-sort-direction normalized-page normalized-page-size]}))
    [:div
      ;; Search input
      (when show-search?
        [search-input table-id search-term])
      
      (cond
        loading?
        [:div {:style {:display "flex" :justify-content "center" :align-items "center" 
                       :padding "4rem" :background "white" :border-radius "12px"
                       :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
         [:div {:style {:text-align "center"}}
          [:div {:style {:width "40px" :height "40px" :border "4px solid #f3f4f6" 
                         :border-top "4px solid #3b82f6" :border-radius "50%"
                         :animation "spin 1s linear infinite" :margin "0 auto 1rem"}}]
          [:div {:style {:color "#6b7280" :font-weight "500"}} "Loading..."]]]

        (or (empty? rows) (= total-count 0))
        [:div {:style {:text-align "center" :padding "4rem" :background "white" 
                       :border-radius "12px" :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
         [:div {:style {:color "#9ca3af" :font-size "1.125rem" :font-weight "500"}}
          (or empty-message "No data found")]
         [:div {:style {:color "#6b7280" :font-size "0.875rem" :margin-top "0.5rem"}}
          "There are no items to display"]]

        :else
        [:div
         ;; Table
         [:div {:style {:background "white" :border-radius "12px" :overflow "hidden"
                        :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)"
                        :border "1px solid #e5e7eb"}}
          [:table {:style {:width "100%" :border-collapse "collapse"}}
           [:thead
            [:tr
             (for [header headers]
               ^{:key (:key header)}
               [sortable-header header table-id sort-config])
             (when actions
               [:th {:style (merge (table-header-style) {:text-align "center"})} "Actions"])]]
           [:tbody
            (for [row rows]  ;; Use rows directly from server
              ^{:key (get row id-key)}
              [:tr {:style {:transition "background-color 0.15s ease-in-out"
                            :hover {:background "#f9fafb"}}}
               (for [header headers]
                 ^{:key (str (get row id-key) "-" (:key header))}
                 [:td {:style (merge (table-cell-style) (:cell-style header))}
                  (if (:render header)
                    ((:render header) (get row (:key header)) row)
                    (str (get row (:key header))))])
               (when actions
                 [:td {:style (merge (table-cell-style) {:text-align "center"})}
                  [:div {:style {:display "flex" :gap "0.5rem" :justify-content "center"}}
                   (for [action actions]
                     ^{:key (:key action)}
                     [action-button (:label action) 
                      #((:on-click action) row)
                      (:variant action)])]])])]]]
         
         ;; Pagination with server pagination data
         (when (and show-pagination? server-pagination)
           [pagination-controls table-id 
            {:current-page (:page server-pagination 0)
             :page-size (:page-size server-pagination 10)} 
            total-count])])]))
