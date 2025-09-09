
(ns features.customizer.panel.frontend.blocks.menu.subs
  (:require
    [re-frame.core :as r]))

;; -----------------------------------------------------------------------------
;; ---- Modal ----

(r/reg-sub
  :customizer.menu/state
  (fn [db [_]]
    (get-in db [:customizer/menu :drawer] false)))

;; ---- Modal ----
;; -----------------------------------------------------------------------------

(r/reg-sub
  :customizer.menu/title
  (fn [db [_  & [fallback-title]]]
    (let [title (get-in db [:customizer/menu :title])]
      (or title fallback-title))))

(r/reg-sub
  :customizer.menu/path
  (fn [db [_]]
    (get-in db [:customizer/menu :path])))
