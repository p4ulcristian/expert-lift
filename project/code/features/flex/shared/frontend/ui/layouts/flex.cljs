(ns features.flex.shared.frontend.ui.layouts.flex
  (:require
   ["react" :as react]
   ["framer-motion" :refer [motion]]
   [re-frame.core :as rf]
   [zero.frontend.react :as zero-react]
   [app.frontend.request :as request]
   
   [ui.modals.zero :as modals]
   [ui.popover-manager :as popover-manager]
   [features.flex.shared.frontend.ui.sidebar :as sidebar]
   [features.flex.shared.frontend.ui.header :as header]
   [ui.notification :as notification]
   [features.flex.zero.frontend.tutorial :as tutorial]
   [zero.frontend.re-frame-viewer.view :as re-frame-viewer]))


(defn background []
  [:div {:style {:position "fixed"
                 :z-index -1
                 :height "100vh"
                 :width "100vw"
                 :background "linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)"}}])

(defn animated-content [current-content animating?]
  [:> (.-div motion)
   {:initial {:opacity 1}
    :animate {:opacity (if animating? 0 1)}
    :transition {:type "spring" :stiffness 200 :damping 20}
    :style {:height "100%"
            :padding "16px 16px 16px 8px"
            :position "relative"
            :will-change "opacity"}}
   [:div {:style {:background "white"
                  :border-radius "16px"
                  :box-shadow "0 4px 20px rgba(0, 0, 0, 0.08), 0 1px 4px rgba(0, 0, 0, 0.04)"
                  :height "100%"
                  :overflow "hidden"
                  :display "flex"
                  :flex-direction "column"}}
    [:div {:style {:padding "32px"
                   :height "100%"
                   :overflow-y "auto"
                   :scrollbar-width "none"
                   :-ms-overflow-style "none"
                   :-webkit-scrollbar {:display "none"}}}
     current-content]]])

(defn use-content-transition-effect
  "Handles content transitions with animation based on current path"
  [content current-path set-current-content set-animating]
  (zero-react/use-effect
   {:mount (fn []
             (if (= current-path "/flex")
               ;; Direct load for /flex route
               (do
                 (set-current-content content)
                 (set-animating false))
               ;; Fade transition for other routes
               (do
                 (set-animating true)
                 ;; Handle the content swap after fade out
                 (js/setTimeout
                  (fn []
                    (set-current-content content)
                    (js/setTimeout #(set-animating false) 25))
                  150)))) ;; Wait for fade out duration
    :params #js [content current-path]}))

(defn content-view 
  "Content view for flex layout (grid column 2)"
  [content]
  (let [[current-content set-current-content] (zero-react/use-state content)
        [animating? set-animating] (zero-react/use-state false)
        current-path @(rf/subscribe [:db/get-in [:router :path]])]
    
    (use-content-transition-effect content current-path set-current-content set-animating)

    [:div {:id    "main-content"
           :style {:grid-column "2"
                   :grid-row "2"
                   :overflow-y "auto"
                   :scrollbar-width "none"
                   :-ms-overflow-style "none"
                   :-webkit-scrollbar {:display "none"}}}
     [animated-content current-content animating?]]))

(defn use-fullscreen-effect
  "Listens for fullscreen changes and updates state accordingly"
  [set-is-fullscreen]
  (zero-react/use-effect
   {:mount (fn []
             (let [fullscreen-change-handler (fn []
                                               (set-is-fullscreen (boolean (.-fullscreenElement js/document))))]
               (.addEventListener js/document "fullscreen-change" fullscreen-change-handler)
               ;; Cleanup function
               (fn []
                 (.removeEventListener js/document "fullscreen-change" fullscreen-change-handler))))
    :params []}))


(defn view
  "Flex layout with sidebar, header and full navigation"
  [{:keys [content]}]
  (let [[is-fullscreen set-is-fullscreen] (react/useState false)
        current-user-data @(rf/subscribe [:user/get])]
    
    ;; Listen for fullscreen changes
    (use-fullscreen-effect set-is-fullscreen)

    
    ;; Initialize re-frame viewer in development (once)
    (react/useEffect 
     #(when ^boolean goog.DEBUG 
        (re-frame-viewer/init!)) 
     #js [])
    
    [tutorial/tutorial-provider
     [:div {:style {:display               "grid"
                    :grid-template-columns "auto 1fr"
                    :grid-template-rows    "auto 1fr"
                    :height                "100vh"
                    :position              "relative"}}
      [background]
      
      ;; Header spans both columns
      [header/view {:user-data current-user-data
                    :is-fullscreen is-fullscreen
                    :grid-column "1 / 3"
                    :grid-row "1"}]

      [sidebar/view {:user-data current-user-data
                     :grid-column "1"
                     :grid-row "2"}]
      
      [content-view content]
      [popover-manager/view {:scroll-ref #(.getElementById js/document "main-content")}]
      [modals/modals]
      [notification/hub {:toastOptions {:duration 1500}}]
      [tutorial/tutorial-controls]
      
      ;; Re-frame viewer (dev only) 
      (when ^boolean goog.DEBUG
        [:<>
         [re-frame-viewer/keyboard-listener]
         [re-frame-viewer/re-frame-viewer]])]]))