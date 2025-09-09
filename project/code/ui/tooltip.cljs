
(ns ui.tooltip
  (:require
   [re-frame.core :as r]
   [zero.frontend.utils :as utils]))

(defn view [{:keys [id tooltip style align anchor duration popover-style]} content]
  (let [tooltip-id (str "tooltip-" id)]
    [:div {:class "tooltip-container"
           :style (merge {:width  "fit-content" 
                          :height "fit-content"}
                         (select-keys (:style (second content)) [:width :height]))
           :on-mouse-leave #(do
                             (if (.contains (.getElementById js/document tooltip-id)
                                            (.-relatedTarget %))
                                (utils/clear-timeout! :tooltip)
                                (utils/set-timeout! :tooltip {:timeout (or duration 1000)
                                                              :fn  (fn [] (r/dispatch [:popover/close tooltip-id]))})))
                            
           :on-mouse-enter (fn [event] 
                             (utils/clear-timeout! :tooltip)
                             (r/dispatch [:popover/open tooltip-id
                                           {:content [:div {:class "tooltip" :style style} tooltip]
                                            :target  (.-target event)
                                            :align   align
                                            :anchor  anchor
                                            :style  (merge {:transition "top .2s, left .2s"} 
                                                           popover-style)}]))}
      content]))
