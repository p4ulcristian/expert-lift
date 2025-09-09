(ns features.flex.zero.frontend.tutorial
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [router.frontend.zero :as router]
   ["@reactour/tour" :refer [TourProvider useTour]]))

(defn add-ws-id-to-url [url]
  (let [wsid @(rf/subscribe [:workspace/get-id])]
    (str "/flex/ws/" wsid "/" url)))

(defn get-workspace-dashboard-url []
  (let [wsid @(rf/subscribe [:workspace/get-id])]
    (str "/flex/ws/" wsid)))

(defn use-tour-hook []
  (useTour))

(defn navigate-to-step [step]
  (cond
    (= step 0) (router/navigate! {:path (get-workspace-dashboard-url)})
    (= step 1) (do (router/navigate! {:path (add-ws-id-to-url "machines")})
                  (js/setTimeout 
                   (fn []
                     ((fn wait-for-element []
                        (if (.getElementById js/document "add-machine-button")
                          nil
                          (js/setTimeout wait-for-element 100))))) 
                   500))
    (= step 2) (router/navigate! {:path (add-ws-id-to-url "processes")})))

(def tutorial-steps
  [{:selector "body"
    :content "Welcome to your business management system! This tutorial will guide you through the key sections. First, let's go to the machines section to see your equipment."
    :action (fn [] (navigate-to-step 0))}
   {:selector "#add-machine-button"
    :content "This is the machines section where you can view and manage all your equipment. Click the 'Add Machine' button to add new equipment to your production line."
    :action (fn [] (navigate-to-step 1))}
   {:selector "#main-content, body" 
    :content "Finally, this is the processes section where you manage your business workflows. You can create, edit, and monitor process execution from this area."
    :action (fn [] (navigate-to-step 2))}])

(defn tutorial-controls []
  (let [tour-api (use-tour-hook)]
    [:div]))

(defn tutorial-provider [children]
  [:> TourProvider
   {:steps (clj->js tutorial-steps)
    :defaultOpen false
    :className "reactour__popover"
    :maskClassName "reactour__mask"
    :highlightedMaskClassName "reactour__mask--highlighted"
    :disableInteraction false
    :disableKeyboardNavigation false
    :disableFocusLock false
    :startAt 0
    :nextButton (fn [^js props]
                  (let [current-step (.-currentStep props)
                        is-last-step (.-isLastStep props)
                        tour-api (use-tour-hook)]
                    (r/as-element
                     [:button {:class "bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
                               :on-click (fn []
                                           (cond
                                             (= current-step 0) (do (router/navigate! {:path (add-ws-id-to-url "machines")})
                                                                    (js/setTimeout 
                                                                     (fn []
                                                                       ((fn wait-for-element []
                                                                          (if (.getElementById js/document "add-machine-button")
                                                                            (.setCurrentStep tour-api 1)
                                                                            (js/setTimeout wait-for-element 100))))) 
                                                                     500))
                                             (= current-step 1) (do (router/navigate! {:path (add-ws-id-to-url "processes")})
                                                                    (js/setTimeout #(.setCurrentStep tour-api 2) 1000))
                                             is-last-step (.setIsOpen tour-api false)
                                             :else (.setCurrentStep tour-api (inc current-step))))}
                      (cond
                        (= current-step 0) "Go to Machines"
                        (= current-step 1) "Go to Processes"
                        is-last-step "Finish"
                        :else "Next")])))
    :prevButton (fn [props]
                  (let [tour-api (use-tour-hook)
                        current-step (.-currentStep props)
                        prev-step (dec current-step)]
                    (r/as-element
                     [:button {:class "bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded mr-2"
                               :on-click (fn []
                                           (cond
                                             (= current-step 1) (do (router/navigate! {:path (get-workspace-dashboard-url)})
                                                                    (js/setTimeout #(.setCurrentStep tour-api 0) 1000))
                                             (= current-step 2) (do (router/navigate! {:path (add-ws-id-to-url "machines")})
                                                                    (js/setTimeout 
                                                                     (fn []
                                                                       ((fn wait-for-element []
                                                                          (if (.getElementById js/document "add-machine-button")
                                                                            (.setCurrentStep tour-api 1)
                                                                            (js/setTimeout wait-for-element 100))))) 
                                                                     500))
                                             :else (.setCurrentStep tour-api prev-step)))}
                      "Previous"])))}
   children])