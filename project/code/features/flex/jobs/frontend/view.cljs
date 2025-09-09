(ns features.flex.jobs.frontend.view
  (:require
   [features.flex.jobs.frontend.request :as jobs-request]
   [clojure.string :as string]
   [features.flex.shared.frontend.components.body :as body]
   [reagent.core :refer [atom]]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.table.zero :as table]
   [ui.text-field :as text-field]
   [utils.time :as time]
   [zero.frontend.re-frame :refer [subscribe]]
   [zero.frontend.react :as zero-react]))

(def search-state (atom {:search/limit 50
                        :search/offset 0
                        :search/text ""
                        :search/order-by :job/created-at}))

(def jobs-state (atom []))

(defn created-at-label [created-at] 
  [:div {:style {:font-size 12}}
   (time/format-date-str created-at)])

(defn job-status-label [status]
  [:div 
   {:style {:background-color (case status 
                                "pending" "#f0ad4e"
                                "active" "#5bc0de"
                                "done" "#5cb85c"
                                "cancelled" "#d9534f"
                                "red")
            :padding "5px"
            :border-radius 10
            :color "#333"
            :text-align :center
            :width "120px"
            :font-size 12 
            :font-weight 800}}
   status])

(defn package-cell [package-name package-picture]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"}}
   (when package-picture
     [:img {:src package-picture
            :style {:width "40px"
                    :height "40px"
                    :object-fit "cover"
                    :border-radius "4px"}}])
   [:div {:style {:font-size 12
                  :font-weight 600
                  :color "#4a5568"}}
    (or package-name "No package")]])

(defn parts-cell [selected-parts]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "4px"
                 :max-width "300px"}}
   (if (and selected-parts (pos? (count selected-parts)))
     [:div
      [:div {:style {:font-size 11
                     :font-weight 600
                     :color "#4a5568"
                     :margin-bottom "4px"}} 
       (str (count selected-parts) " parts:")]
      [:div {:style {:display "flex"
                     :flex-wrap "wrap"
                     :gap "6px"}}
       (for [part selected-parts]
         ^{:key (:id part)}
         [:div {:style {:display "flex"
                        :align-items "center"
                        :gap "4px"
                        :padding "2px 6px"
                        :background "#f7fafc"
                        :border-radius "4px"
                        :border "1px solid #e2e8f0"}}
          (when (:picture_url part)
            [:img {:src (:picture_url part)
                   :style {:width "24px"
                           :height "24px"
                           :object-fit "cover"
                           :border-radius "2px"}}])
          [:span {:style {:font-size 10
                          :color "#4a5568"}}
           (str (:name part) " (x" (:quantity part) ")")]])]]
     [:div {:style {:font-size 11
                    :color "#a0aec0"}}
      "No parts selected"])])

(defn navigate-to-job [job-id]
  (let [wsid @(subscribe [:workspace/get-id]) 
        url (str "/flex/ws/" wsid "/jobs/" job-id)]
    (router/navigate! {:path url})))

(defn jobs [] 
  [table/view {:rows @jobs-state
               :grid-template-columns "auto auto 1fr auto auto auto"
               :columns [:package_name :selected_parts :description :created_at :status :inspect-button]
               :labels   {:package_name "Package"
                         :selected_parts "Parts"
                         :description "Description"}
               :column-elements {:created_at created-at-label
                                :status     job-status-label
                                :package_name (fn [package-name row]
                                               (package-cell package-name (:package_picture row)))
                                :selected_parts parts-cell
                                :description (fn [description]
                                             [:div {:style {:font-size 12
                                                           :max-width "200px"
                                                           :overflow "hidden"
                                                           :text-overflow "ellipsis"}}
                                              [:div description]])
                                :inspect-button (fn [_ row]
                                                 [button/view
                                                  {:color "#a8c0d6"
                                                   :on-click #(navigate-to-job (:id row))}
                                                  "Inspect"])
                                }
               :row-element (fn [style content]
                             [:div {:style (merge style {:padding "8px"})}
                              content])}])

(defn get-my-jobs []
  (let [workspace-id @(subscribe [:workspace/get-id])]
    (jobs-request/get-jobs
     workspace-id
     (fn [response]
       (reset! jobs-state response)))))

(defn search-jobs []
  [:div {:style {:padding 10}}
   [text-field/view
    {:value (:search/text @search-state)
     :on-change #(swap! search-state assoc :search/text %)
     :on-type-ended {:fn  (fn [] (get-my-jobs))}
     :placeholder "Search jobs..."
     :style {:width "100%"}}]])

(defn jobs-content []
  [:div
   [search-jobs]
   [jobs]])

(defn view []
  (zero-react/use-effect
   {:mount (fn []
             (get-my-jobs))})
  [body/view
   {:title "Jobs"
    :body [jobs-content]}]) 