(ns ui.enhanced-button)

(defn- button-variant-styles [variant disabled? loading?]
  "Get styles based on button variant"
  (let [base-styles {:padding "0.75rem 1.25rem" :border "none" :border-radius "8px" 
                     :cursor (if (or disabled? loading?) "not-allowed" "pointer")
                     :font-weight "600" :font-size "0.875rem" :letter-spacing "0.025em"
                     :transition "all 0.2s ease-in-out" :box-shadow "0 1px 2px 0 rgba(0, 0, 0, 0.05)"
                     :opacity (if (or disabled? loading?) 0.7 1)}]
    (merge base-styles
           (case variant
             :primary {:background (if (or disabled? loading?) "#9ca3af" "#3b82f6") 
                       :color "white"
                       :hover (when-not (or disabled? loading?) {:background "#2563eb" 
                                                                 :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"})}
             :secondary {:background (if (or disabled? loading?) "#f3f4f6" "white") 
                         :color "#374151" :border "1px solid #d1d5db"
                         :hover (when-not (or disabled? loading?) {:background "#f9fafb" :border-color "#9ca3af"})}
             :success {:background (if (or disabled? loading?) "#9ca3af" "#10b981") 
                       :color "white"
                       :hover (when-not (or disabled? loading?) {:background "#059669" 
                                                                 :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"})}
             :danger {:background (if (or disabled? loading?) "#9ca3af" "#dc2626") 
                      :color "white"
                      :hover (when-not (or disabled? loading?) {:background "#b91c1c"
                                                                :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"})}
             :warning {:background (if (or disabled? loading?) "#9ca3af" "#f59e0b") 
                       :color "white"
                       :hover (when-not (or disabled? loading?) {:background "#d97706"
                                                                 :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"})}
             ;; default to secondary
             {:background (if (or disabled? loading?) "#f3f4f6" "white") 
              :color "#374151" :border "1px solid #d1d5db"
              :hover (when-not (or disabled? loading?) {:background "#f9fafb" :border-color "#9ca3af"})}))))

(defn enhanced-button
  "Enhanced button component with variants, loading states, and icons"
  [{:keys [variant on-click disabled? loading? icon children text size full-width?]
    :or {variant :secondary size :md}}]
  (let [size-styles (case size
                      :sm {:padding "0.5rem 0.75rem" :font-size "0.75rem"}
                      :lg {:padding "1rem 1.5rem" :font-size "1rem"}
                      {:padding "0.75rem 1.25rem" :font-size "0.875rem"})
        button-styles (merge (button-variant-styles variant disabled? loading?)
                            size-styles
                            (when full-width? {:width "100%"}))]
    [:button {:type "button"
              :disabled (or disabled? loading?)
              :on-click (when (and on-click (not disabled?) (not loading?)) on-click)
              :style button-styles}
     [:div {:style {:display "flex" :align-items "center" :justify-content "center" :gap "0.5rem"}}
      (when loading?
        [:div {:style {:width "1rem" :height "1rem" :border "2px solid transparent"
                       :border-top "2px solid currentColor" :border-radius "50%"
                       :animation "spin 1s linear infinite"}}])
      (when (and icon (not loading?))
        [:span {:style {:display "flex" :align-items "center"}} icon])
      [:span (or text children)]]]))

(defn button-group
  "Group of buttons with consistent spacing"
  [{:keys [gap align]} & buttons]
  [:div {:style {:display "flex" 
                 :gap (or gap "0.75rem")
                 :justify-content (case align
                                   :left "flex-start"
                                   :right "flex-end"
                                   :center "center"
                                   "flex-start")}}
   (for [button buttons]
     button)])

(defn icon-button
  "Icon-only button variant"
  [{:keys [icon variant on-click disabled? loading? size tooltip]
    :or {variant :secondary size :md}}]
  (let [size-styles (case size
                      :sm {:padding "0.5rem" :width "2rem" :height "2rem"}
                      :lg {:padding "0.75rem" :width "3rem" :height "3rem"}
                      {:padding "0.625rem" :width "2.5rem" :height "2.5rem"})
        button-styles (merge (button-variant-styles variant disabled? loading?)
                            size-styles
                            {:display "flex" :align-items "center" :justify-content "center"})]
    [:button {:type "button"
              :disabled (or disabled? loading?)
              :on-click (when (and on-click (not disabled?) (not loading?)) on-click)
              :title tooltip
              :style button-styles}
     (if loading?
       [:div {:style {:width "1rem" :height "1rem" :border "2px solid transparent"
                      :border-top "2px solid currentColor" :border-radius "50%"
                      :animation "spin 1s linear infinite"}}]
       icon)]))