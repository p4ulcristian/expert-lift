
(ns ui.notification
  (:require
   ["react-hot-toast" :as toast]
   [reagent.core      :as reagent]
   [re-frame.core     :as r]))

(defn toast-promise! [promise ^js props]
  (.promise toast/toast promise props))

(defn toast! [type message & [^js props]]
  (try
    (case type
      :success (.success toast/toast message props)
      :error   (.error   toast/toast (reagent/as-element message) props)
      :custom  (.custom  toast/toast (reagent/as-element message) props)
      :loading (.loading toast/toast (reagent/as-element message) props)
      (toast/toast (reagent/as-element message) props))
    (catch :default e
      (js/console.error e))))

(defn loading! [id message]
  (toast! :loading message #js{"id" id "duration" js/Infinity}))

(defn success! [id message]
  (toast! :success message #js{"id" id "duration" 1000}))

(defn error! [id message]
  (toast! :error message #js{"id" id "duration" 1000}))

(r/reg-fx
  :notifications/notify
  (fn [[type message & [props]]]
    (toast! type message props)))

(r/reg-event-fx
  :notifications/notify!
  (fn [_ [_ type message & [props]]]
    {:notifications/notify [type message props]}))

(r/reg-event-fx
  :notifications/loading!
  (fn [_ [_ id message & [duration]]]
    {:notifications/notify [:loading message #js{"id" id "duration" (or duration js/Infinity)}]}))

(r/reg-event-fx
  :notifications/success!
  (fn [_ [_ id message & [duration]]]
    {:notifications/notify [:success message #js{"id" id "duration" (or duration 1000)}]}))

(r/reg-event-fx
  :notifications/error!
  (fn [_ [_ id message & [duration]]]
    {:notifications/notify [:error message #js{"id" id "duration" (or duration 1000)}]}))

(defn hub [& [override]]
  [:> toast/Toaster (merge {:position     "bottom-left"
                            :gutter       4
                            :toastOptions {:duration 1500}}
                           override)])


;; Docs:
;; https://react-hot-toast.com/docs

;; Demo:
;; (notification/toast! :success "Success")
;; (notification/toast! :warning "Warning")
;; (notification/toast! :error "Error")
;; (notification/toast! :info "Info")
;; (notification/toast! :success "Success" #js{"style" #js{"background" "my-color"}})