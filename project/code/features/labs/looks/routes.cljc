(ns features.labs.looks.routes
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware]))
  #?(:cljs (:require [features.labs.looks.frontend.editor.views :as looks.editor]
                     [features.labs.looks.frontend.lister.views :as looks.lister])))

(def list-path      "/irunrainbow/looks")
(def editor-path    "/irunrainbow/looks/editor")
(def edit-item-path "/irunrainbow/looks/editor/:id")

(def routes
  #?(:clj  [{:path list-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}
            {:path editor-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}
            {:path edit-item-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]
     :cljs [{:path list-path
             :view #'looks.lister/view}
            {:path editor-path
             :view #'looks.editor/view}
            {:path edit-item-path
             :view #'looks.editor/view}]))