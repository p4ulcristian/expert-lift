(ns ui.content-section)

(def section-container-style
  "Full page container with background"
  {:height "calc(100vh - 60px)"
   :overflow "auto"
   :background "#f9fafb"})

(def section-content-style
  "Centered content area - max 800px"
  {:max-width "800px"
   :margin "0 auto"
   :padding "2rem 1rem"})

(defn content-section
  "Reusable content section with centered content container.
   Uses semantic <section> tag for better accessibility.
   Provides consistent styling across all feature pages."
  [& children]
  [:section {:style section-container-style}
   [:div {:style section-content-style}
    (for [[index child] (map-indexed vector children)]
      ^{:key index} child)]])
