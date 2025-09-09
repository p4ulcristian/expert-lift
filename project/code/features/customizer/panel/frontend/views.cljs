
(ns features.customizer.panel.frontend.views
  (:require
   [re-frame.core          :as r]
   ["react"                :as react]

   [features.customizer.blocks.local-storage]
   [features.customizer.panel.frontend.effects]
   [features.customizer.panel.frontend.blocks.url]
   [features.customizer.panel.frontend.subs]
   
   [features.customizer.blocks.cart :as cart]
   [features.customizer.blocks.my-designs :as my-designs]
   [features.customizer.blocks.details :as details]
   [features.customizer.panel.frontend.blocks.cart-dialog :as cart-dialog]

   [features.customizer.panel.frontend.views-desktop :as desktop]))

;;    [zero.frontend.reveal :as reveal]))

;; (reveal/inspect-db)

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn- layout-handler [layout]
  (case layout
    "desktop" [desktop/view]))
    ;; "mobile"  [mobile.views/view]))

(defn- load-local-storage [cart-content]
  (react/useEffect 
    (fn []
      (when (nil? cart-content)
        (r/dispatch [:local-storage/load-item [:cart :content] "customizer-cart-content"]))
      (fn []))
    #js[]))

(defn watch-cart-content [cart-content]
  (react/useEffect
    (fn []
      (when-not @(r/subscribe [:db/get-in [:customizer :initializing?] true])
        (r/dispatch [:local-storage/set-item! "customizer-cart-content" cart-content]))
      (fn []))
    #js[cart-content]))

(defn- init-customizer [menu-id]
  (react/useEffect
    (fn []
      (try (r/dispatch [:customizer/init! menu-id])
           (catch :default e
             (println "Error initializing customizer:" e)))
      (fn []))
    #js[menu-id]))

(defn view []
  (let [layout "desktop"
        menu-id      @(r/subscribe [:db/get-in [:router :query-params :menu-id]])
        cart-content @(r/subscribe [:db/get-in [:cart :content]])] 
    (load-local-storage cart-content)
    (watch-cart-content cart-content)
    (init-customizer menu-id)
    
    
    [:<>
     [:div {:id          "customizer"
            :data-layout layout}
      [layout-handler layout]
      [cart/view]
      [my-designs/view]
      [details/view]
      [cart-dialog/view]]]))

