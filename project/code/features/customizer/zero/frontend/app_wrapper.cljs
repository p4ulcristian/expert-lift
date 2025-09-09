(ns features.customizer.zero.frontend.app-wrapper
  (:require
   [features.flex.shared.frontend.ui.sidebar    :as sidebar]
   [ui.modals.zero     :as modals]
   [ui.popover-manager :as popover-manager]
   [ui.notification    :as notification]))

(defn content-view [content]
  [:div {:style {:padding "10px"
                 :height "100vh"
                 :overflow-y "auto"}}
   content])

(defn background []
  [:img {:style {:position :fixed
                 :z-index -1
                 :height "100vh"
                 :width "100vw"
                 :object-fit "cover"}
         :src "/images/background.svg"}])

(defn view [content]
  [:div {:style {:display               "grid"
                 :grid-template-columns "auto 1fr"
                 :height                "100vh"}}
   [background]
   [sidebar/view]
   [content-view content]
   [popover-manager/view]
   [modals/modals]
   [notification/hub]])
