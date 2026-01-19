(ns ui.data-table.search
  "Search field component for data tables.
   Provides a universal search input with debounced search execution."
  (:require
   [ui.data-table.icons :as icons]
   [ui.button :as button]
   [ui.text-field :as text-field]))

(defn search-clear-button
  "Clear search button"
  [{:keys [value on-clear]}]
  (when (seq value)
    [button/view {:mode     :clear_2
                  :type     :secondary
                  :class    "data-table-search-clear-button"
                  :override {:title "Clear Search"}
                  :on-click on-clear}
      [:i {:class (icons/icon :close)}]]))

(defn search-field
  "Universal search field component for data tables.

  Props:
    :search-term         - Current search term value (string)
    :on-search-change    - (fn [value]) called when user types
    :on-search           - (fn [value]) called when search should execute (debounced)
    :placeholder         - Optional placeholder text (default: 'Search by name or description...')
    :container-class     - Optional container CSS class (default: 'data-table-search-container')
    :input-class         - Optional input CSS class (default: 'data-table-search-input')"
  [{:keys [search-term
           on-search-change
           on-search
           placeholder
           container-class
           input-class]}]
  (let [placeholder     (or placeholder "Search by name or description...")
        container-class (or container-class "data-table-search-container")
        input-class     (or input-class "data-table-search-input")]
    [:div {:class container-class}
     [text-field/view
       {:class           input-class
        :placeholder     placeholder
        :value           (or search-term "")
        :on-change       on-search-change
        :on-type-ended   on-search
        :left-adornment  [:i {:class (icons/icon :search)}]
        :right-adornment [search-clear-button
                          {:value    search-term
                           :on-clear #(do
                                        (when on-search-change
                                          (on-search-change ""))
                                        (when on-search
                                          (on-search "")))}]}]]))

(defn view
  "Main entry point for search field component.
   See search-field for full documentation."
  [props]
  [search-field props])
