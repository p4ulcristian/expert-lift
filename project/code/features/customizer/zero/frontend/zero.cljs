(ns features.customizer.zero.frontend.zero
  (:require
   ["react" :as react]
   [app.frontend.request]
   [features.customizer.checkout.routes      :as checkout.routes]
   [features.customizer.panel.routes         :as customizer.routes]
   [features.customizer.test-3d.routes       :as test-3d.routes]
   [router.frontend.zero :as router]
   [ui.modals.zero     :as modals]
   [ui.popover-manager :as popover-manager]
   [ui.notification    :as notification]
   [zero.frontend.re-frame]))


(defn window-resize-hook []
  (let [[window-size set-window-size] (react/useState (.-innerWidth js/window))
        handle-resize (fn [e]
                       (set-window-size (.-innerWidth js/window)))]
    (react/useEffect
      (fn []
        (.addEventListener js/window "resize" handle-resize)
        (fn []
          (.removeEventListener js/window "resize" handle-resize)))
      #js[])
    window-size))

(def routes (concat
              customizer.routes/routes
              checkout.routes/routes
              test-3d.routes/routes))

(defn view []
  (let [router-data  (:data @router/state)
        window-width (window-resize-hook)]
     ;; This is the router component and the component-params 
     ;; are passed to the component 
     ;; Wrapper + router-component + component-params
    [:<>
      [:main {:id "main-content"}
        [(:view router-data)]]
      [popover-manager/view {:scroll-ref #(.getElementById js/document "main-content")}]
      [modals/modals]
      [notification/hub {:gutter       4
                         :position     (if (>= window-width 600) "bottom-left" "top-center")
                         :toastOptions #js{:duration 2000
                                           :style    #js{:background "rgb(64 64 64 / 90%)"
                                                         :border     "1px solid var(--irb-clr)"
                                                         :color      "#fff"
                                                         :min-width  (if (>= window-width 600) "auto" "100%")}}}]]))
