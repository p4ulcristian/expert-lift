(ns features.flex.recipes.frontend.recipe
  (:require
   [features.flex.recipes.frontend.request :as recipes-request]
   [features.flex.processes.frontend.request :as processes-request]
   [features.flex.shared.frontend.components.body :as edit-page]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.text-field :as text-field]
   [ui.textarea :as textarea]
   [zero.frontend.react :as zero-react]))

(defn- initialize-new-recipe []
  "Initialize state for new recipe"
  (r/dispatch [:db/assoc-in [:flex/recipe] {}])
  (r/dispatch [:db/assoc-in [:flex/recipe :recipe/name] ""])
  (r/dispatch [:db/assoc-in [:flex/recipe :recipe/description] ""])
  (r/dispatch [:db/assoc-in [:flex/recipe :recipe/process-ids] []]))

(defn- handle-recipe-response [recipe]
  "Handle response from get-recipe request"
  (let [processes (:recipe/processes recipe)
        sorted-processes (sort-by :recipe-process/step-order processes)
        process-ids (map :process/id sorted-processes)]
    (r/dispatch [:db/assoc-in [:flex/recipe] recipe])
    (r/dispatch [:db/assoc-in [:flex/recipe :recipe/process-ids] process-ids])))

(defn get-recipe-data [recipe-id]
  "Load recipe data or initialize for new recipe"
  (if (= recipe-id "new")
    (initialize-new-recipe)
    (let [wsid @(r/subscribe [:workspace/get-id])]
      (recipes-request/get-recipe wsid recipe-id handle-recipe-response))))

(defn get-processes []
  "Load all processes for recipe selection"
  (let [wsid @(r/subscribe [:workspace/get-id])]
    (processes-request/get-processes wsid
      (fn [processes]
        (r/dispatch [:db/assoc-in [:flex/processes :list] processes])))))

(defn reorder-processes [processes from-idx to-idx]
  (let [processes-vec (vec processes)
        item (nth processes-vec from-idx)
        without-item (vec (concat (subvec processes-vec 0 from-idx)
                                (subvec processes-vec (inc from-idx))))
        before-target (subvec without-item 0 to-idx)
        after-target (subvec without-item to-idx)]
    (vec (concat before-target [item] after-target))))

(defn process-selector []
  (let [selected-processes @(r/subscribe [:db/get-in [:flex/recipe :recipe/process-ids] []])
        all-processes @(r/subscribe [:db/get-in [:flex/processes :list] []])]
    [:div
     [:label {:style {:display "block"
                     :margin-bottom "8px"
                     :color "#333"
                     :font-weight "500"}}
      "Processes"]
     [:div {:style {:display "flex"
                   :flex-direction "column"
                   :gap "8px"}}
      ;; Selected processes display
      (when (seq selected-processes)
        [:div {:style {:display "flex"
                      :flex-direction "column"
                      :gap "8px"
                      :padding "8px"
                      :border "1px solid #ddd"
                      :border-radius "4px"
                      :background-color "#f9f9f9"}}
         (for [[idx process-id] (map-indexed vector selected-processes)
               :let [process (first (filter #(= (:id %) process-id) all-processes))]]
           ^{:key (str process-id "-" idx)}
           [:div {:style {:display "flex"
                         :align-items "center"
                         :gap "8px"
                         :padding "8px"
                         :background-color "white"
                         :border "1px solid #ddd"
                         :border-radius "4px"
                         :cursor "move"}
                 :draggable true
                 :on-drag-start #(.. % -dataTransfer (setData "text/plain" (str idx)))
                 :on-drag-over (fn [e] (.preventDefault ^js e))
                 :on-drop (fn [e]
                           (let [from-idx (js/parseInt (.. e -dataTransfer (getData "text/plain")))
                                 to-idx idx
                                 new-order (reorder-processes selected-processes from-idx to-idx)]
                             (r/dispatch [:db/assoc-in [:recipe :process_ids] new-order])))}
            [:i {:class "fa-solid fa-grip-vertical"
                 :style {:color "#666"
                        :margin-right "8px"}}]
            [:span (:name process)]
            [:button {:style {:border "none"
                            :background "none"
                            :color "#666"
                            :cursor "pointer"
                            :padding "0 4px"
                            :margin-left "auto"}
                     :on-click (fn [_] 
                                 (let [updated-processes (vec (concat (take idx selected-processes)
                                                                    (drop (inc idx) selected-processes)))]
                                   (r/dispatch [:db/assoc-in [:recipe :process_ids] updated-processes])))}
             [:i {:class "fa-solid fa-xmark"}]]])])
      
      ;; Process selection dropdown
      [:select {:value ""
                :style {:width "100%"
                       :padding "8px"
                       :border "1px solid #ddd"
                       :border-radius "4px"
                       :font-size "14px"}
                :on-change #(let [selected-id (.. ^js % -target -value)]
                            (when (not= selected-id "")
                              (r/dispatch [:db/assoc-in [:recipe :process_ids] 
                                         (conj (or selected-processes []) selected-id)])))}
       [:option {:value ""} "Select a process to add..."]
       (for [process all-processes]
         [:option {:key (:id process)
                  :value (:id process)}
          (:name process)])]]]))


(defn- navigate-to-recipes [wsid]
  "Navigate back to recipes list"
  (router/navigate! {:path (str "/flex/ws/" wsid "/recipes")}))

(defn- prepare-recipe-data [recipe-data recipe-id]
  "Prepare recipe data for submission"
  (let [base-data {:name (:name recipe-data)
                   :description (:description recipe-data)
                   :process_ids (:process_ids recipe-data)}]
    (if (= recipe-id "new")
      base-data
      (assoc base-data :id recipe-id))))

(defn handle-submit []
  "Handle recipe form submission"
  (let [recipe-data @(r/subscribe [:db/get-in [:recipe]])
        wsid @(r/subscribe [:workspace/get-id])
        recipe-id @(r/subscribe [:db/get-in [:router :path-params :recipe-id]])
        prepared-data (prepare-recipe-data recipe-data recipe-id)]
    (if (= recipe-id "new")
      (recipes-request/create-recipe wsid prepared-data #(navigate-to-recipes wsid))
      (recipes-request/edit-recipe wsid prepared-data #(navigate-to-recipes wsid)))))

(defn recipe-form []
  (let [recipe-id @(r/subscribe [:db/get-in [:router :path-params :recipe-id]])]
    [:div {:style {:max-width "600px"
                   :margin "0 auto"
                   :padding-top "32px"
                   :display "flex"
                   :flex-direction "column"
                   :gap "32px"}}
     [:div {:style {:display "grid" 
                   :grid-template-columns "1fr" 
                   :gap "24px"}}
      [text-field/view {:label "Name"
                       :value @(r/subscribe [:db/get-in [:recipe :name] ""])
                       :on-change #(r/dispatch [:db/assoc-in [:recipe :name] %])}]
      [textarea/view {:label "Description"
                     :value @(r/subscribe [:db/get-in [:recipe :description] ""])
                     :on-change #(r/dispatch [:db/assoc-in [:recipe :description] %])
                     :rows 6}]
      [process-selector]]
     [:div {:style {:text-align "center"}}
      [button/view {:mode :filled
                   :color "var(--seco-clr)"
                   :on-click handle-submit}
       (if (= recipe-id "new") "Add Recipe" "Save Changes")]]]))

(defn recipe []
  (let [recipe-id @(r/subscribe [:db/get-in [:router :path-params :recipe-id]])
        wsid @(r/subscribe [:workspace/get-id])]
    [edit-page/view
     {:title (if (= recipe-id "new") "New Recipe" "Edit Recipe")
      :description "Create, edit, and remove recipes."
      :title-buttons (list
                      (when (and recipe-id (not= recipe-id "new"))
                        ^{:key "delete"}
                        [button/view {:mode :outlined
                                     :color "var(--seco-clr)"
                                     :style {:fontWeight 500 
                                            :padding "8px 20px"}
                                     :on-click #(recipes-request/delete-recipe wsid recipe-id 
                                                  (fn [_] (navigate-to-recipes wsid)))}
                         "Delete"])
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/recipes")})}
                       "Back"])
      :body [recipe-form]}]))

(defn view []
  (let [recipe-id (r/subscribe [:db/get-in [:router :path-params :recipe-id]])]
    (zero-react/use-effect
     {:mount (fn []
               (get-processes)
               (get-recipe-data @recipe-id))})
    [:div
     [recipe]])) 