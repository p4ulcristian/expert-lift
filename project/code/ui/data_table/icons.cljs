(ns ui.data-table.icons
  "Simple icon helpers for data table components.
   Uses Font Awesome classes directly.")

(def icons
  {:edit    "fa-solid fa-pen"
   :delete  "fa-solid fa-trash"
   :close   "fa-solid fa-xmark"
   :search  "fa-solid fa-magnifying-glass"
   :loading "fa-solid fa-spinner"
   :check   "fa-solid fa-check"})

(defn icon
  "Get icon class by keyword"
  [icon-key]
  (get icons icon-key "fa-solid fa-circle"))
