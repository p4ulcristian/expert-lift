(ns features.flex.workspaces.frontend.view 
  (:require
   [parquery.frontend.request :as parquery]
   [re-frame.core :as rf]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.card :as card]
   [ui.text-field :as text-field]
   [utils.time :as time]
   [zero.frontend.re-frame :refer [dispatch subscribe]]
   [zero.frontend.react :as zero-react]))

;; Re-frame events
(rf/reg-event-db
 ::set-workspaces
 (fn [db [_ workspaces]]
   (let [grouped-workspaces (group-by :workspace/role workspaces)
         owned-workspaces (get grouped-workspaces "owner" [])
         shared-workspaces (get grouped-workspaces "employee" [])]
     (-> db
         (assoc-in [:workspaces :owned-workspaces] owned-workspaces)
         (assoc-in [:workspaces :shared-workspaces] shared-workspaces)))))

(rf/reg-event-db
 ::set-workspace-name
 (fn [db [_ name]]
   (assoc-in db [:workspaces :workspace-name] name)))

(rf/reg-event-db
 ::clear-workspace-name
 (fn [db _]
   (assoc-in db [:workspaces :workspace-name] "")))

;; Re-frame subscriptions
(rf/reg-sub
 ::owned-workspaces
 (fn [db _]
   (get-in db [:workspaces :owned-workspaces] [])))

(rf/reg-sub
 ::shared-workspaces
 (fn [db _]
   (get-in db [:workspaces :shared-workspaces] [])))

(rf/reg-sub
 ::workspace-name
 (fn [db _]
   (get-in db [:workspaces :workspace-name] "")))

(rf/reg-sub
 ::all-workspaces
 :<- [::owned-workspaces]
 :<- [::shared-workspaces]
 (fn [[owned shared] _]
   (concat owned shared)))

(rf/reg-sub
 ::first-owned-workspace?
 :<- [::owned-workspaces]
 (fn [owned-workspaces _]
   (empty? owned-workspaces)))


(defn get-my-workspaces []
  (parquery/send-queries
   {:queries {:workspaces/get-my-workspaces {}}
    :parquery/context {}
    :callback (fn [response]
                (let [my-workspaces (:workspaces/get-my-workspaces response)]
                  (dispatch [::set-workspaces my-workspaces])))}))

(defn redirect-to-workspace [{:workspace/keys [id]
                              :as _response}]
  (router/navigate! {:path (str "/flex/ws/" id)}))

(defn add-workspace []
  (let [workspace-name @(subscribe [::workspace-name])]
    (parquery/send-queries
     {:queries {:workspaces/add-workspace {:workspace/name workspace-name}}
      :parquery/context {}
      :callback (fn [response]
                  (dispatch [::clear-workspace-name])
                  (redirect-to-workspace (:workspaces/add-workspace response))
                  (get-my-workspaces)
                  response)})))


(defn remove-workspace [workspace-id]
  (parquery/send-queries
   {:queries {:workspaces/remove-workspace {:workspace/id workspace-id}}
    :parquery/context {}
    :callback (fn [response]
                (get-my-workspaces)
                (println response))}))


(defn workspace [w]
  [card/view
   {:on-click  (fn [] (redirect-to-workspace w))
    :on-delete (fn [] (remove-workspace (:workspace/id w)))
    :style {:width "240px"
            :min-height "100px"}
    :content [:div
              {:style {:text-align :left
                       :padding "16px"
                       :display :flex
                       :flex-direction :column
                       :justify-content :center
                       :align-items :flex-start
                       :height "100%"}}
              [:div {:style {:font-size "16px"
                             :font-weight "600"
                             :color "#2c3e50"
                             :margin-bottom "10px"
                             :line-height "1.3"}}
               (:workspace/name w)]
              [:div {:style {:font-size "12px"
                            :color "#7f8c8d"
                            :font-weight "400"}}
               (str "Created " (time/format-date-no-time (:workspace/created-at w)))]]}])

(defn my-workspaces []
  (let [owned-workspaces @(subscribe [::owned-workspaces])
        shared-workspaces @(subscribe [::shared-workspaces])]
    [:div
     ;; Owner workspaces section
     (when (seq owned-workspaces)
       [:div {:style {:margin-bottom "32px"}}
        [:h2 {:style {:font-size "18px"
                      :font-weight "600"
                      :margin-bottom "16px"
                      :color "#2c3e50"
                      :border-bottom "2px solid #3498db"
                      :padding-bottom "8px"}}
         "Your Workspaces (Owner)"]
        [:div {:style {:display :flex
                       :flex-wrap :wrap
                       :justify-content :center
                       :gap "20px"}}
         (map
          (fn [w]
            ^{:key (:workspace/id w)} [workspace w])
          owned-workspaces)]])
     
     ;; Member workspaces section  
     (when (seq shared-workspaces)
       [:div {:style {:margin-bottom "32px"}}
        [:h2 {:style {:font-size "18px"
                      :font-weight "600"
                      :margin-bottom "16px"
                      :color "#2c3e50"
                      :border-bottom "2px solid #27ae60"
                      :padding-bottom "8px"}}
         "Shared Workspaces (Member)"]
        [:div {:style {:display :flex
                       :flex-wrap :wrap
                       :justify-content :center
                       :gap "20px"}}
         (map
          (fn [w]
            ^{:key (:workspace/id w)} [workspace w])
          shared-workspaces)]])]))

(defn joined-workspaces-newcomer [{:user/keys [email]}]
  [:div {:style {:border "1px solid #ddd"
                 :border-radius "12px"
                 :padding "24px"
                 :box-shadow "0 2px 8px rgba(0, 0, 0, 0.05)"}}
   [:h2 {:style {:font-size "20px"
                 :margin-bottom "12px"}}
    "Join an Existing Workspace"]
   [:p {:style {:font-size "14px"
                :margin-bottom "8px"}}
    "Your registered email: " [:strong email]]
   [:p {:style {:font-size "14px"
                :margin-bottom "8px"}}
    "To join a workspace, share this email with your workspace manager. They'll send you an invitation — just follow the instructions in your inbox."]])

(defn info-bulb []
  [:div {:style {:border "1px solid #eee"
                 :border-radius "12px"
                 :background-color "#f9f9f9"
                 :padding "20px"
                 :width "200px"}}
   [:h3 {:style {:font-size "16px"
                 :margin-bottom "8px"
                 :font-weight "600"}}
    "💡 What is a Workspace?"]
   [:p {:style {:font-size "14px"
                :line-height "1.5"}}
    "A workspace represents one of your powder coating facilities. If your business operates in multiple locations, create a separate workspace for each site."]])


(defn info-popover [] 
  [:div {:style {:background :white 
                 :display :flex 
                 :justify-content :center
                 :align-items :center
                 :height 40
                 :width 40
                 :position :absolute
                 :top 10
                 :right 10
                 :border-radius "50%"}}
   [:i {:on-mouse-over 
        (fn [e] 
          (dispatch [:popover/open 
                     :workspace/info
                     {:align [:bottom :left]
                      :anchor [:top :center]
                      :target (.-target e)
                      :content [info-bulb]}]))
        :on-mouse-leave (fn [_] 
                          (dispatch [:popover/close :workspace/info]))
        :class "fa-solid fa-lightbulb"
        :style {:font-size "20px"
                :color "#f39c12"
                :cursor "pointer"}}]])

(defn my-workspaces-setup-help-text []
  (let [first-owned-workspace? @(subscribe [::first-owned-workspace?])]
    [:p {:style {:font-size "14px"
                 :line-height "1.5"}}
     (if first-owned-workspace? 
       "Create a new workspace for your facility. Perfect if you're managing a new location or setting things up for the first time."
       "Need to manage a new location or team? Set up an additional workspace to keep things organized.")]))

(defn my-workspaces-setup-title []
  (let [first-owned-workspace? @(subscribe [::first-owned-workspace?])]
    [:h2 {:style {:font-size "20px"
                  :margin-bottom "12px"}}
     (if first-owned-workspace? 
       "Set Up a Workspace"
       "Create Another Workspace")]))

(defn my-workspaces-setup []
  (let [workspace-name @(subscribe [::workspace-name])
        first-owned-workspace? @(subscribe [::first-owned-workspace?])]
    [:div {:style {:border "1px solid #ddd"
                   :border-radius "12px"
                   :padding "24px"
                   :position :relative
                   :box-shadow "0 2px 8px rgba(0, 0, 0, 0.05)"}}
     [my-workspaces-setup-title]
     ;fontawesome icon with a bulb 
     [info-popover]
     
     [my-workspaces-setup-help-text]
     [:div {:style {:display :grid
                    :place-items :center
                    :grid-template-columns "1fr"
                    :gap 10
                    :margin-top 20}}
      [text-field/view {:on-change (fn [value] (dispatch [::set-workspace-name value]))
                        :value workspace-name
                        :placeholder "Workspace name"}]
      [button/view {:disabled (= "" workspace-name)
                    :on-click add-workspace}
       (if first-owned-workspace? 
         "Add your first workspace"
         "Create Another Workspace")]]]))

(defn welcome-user [{:user/keys [name]}]
  [:h1
   {:style {:font-size "35px"
            :text-align "center"}}
   (str "Welcome " name " !")])


(defn view []
  (zero-react/use-effect
   {:mount (fn []
             (dispatch [:workspace/set nil])
             (parquery/send-queries
              {:queries {:workspaces/get-my-workspaces {}}
               :parquery/context {}
               :callback (fn [response]
                           (let [my-workspaces (:workspaces/get-my-workspaces response)]
                             (dispatch [::set-workspaces my-workspaces])))}))})
  [:div {:style {:display :grid
                 :grid-template-columns "1fr"
                 :gap "24px"
                 :padding "40px"
                 :max-width "600px"
                 :margin "0 auto"
                 :color "#333"}} 
   [:h1 {:style {:font-size "35px"
                 :text-align "center"}}
    "Welcome!"]
   [my-workspaces-setup]
   [my-workspaces]
   [:div {:style {:border "1px solid #ddd"
                  :border-radius "12px"
                  :padding "24px"
                  :box-shadow "0 2px 8px rgba(0, 0, 0, 0.05)"}}
    [:h2 {:style {:font-size "20px"
                  :margin-bottom "12px"}}
     "Join an Existing Workspace"]
    [:p {:style {:font-size "14px"
                 :margin-bottom "8px"}}
     "To join a workspace, ask your workspace manager to send you an invitation."]]])