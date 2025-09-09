(ns authentication.routes
  (:require 
   [authentication.authorize.flex :as flex-authorize]
   [authentication.authorize.customizer :as customizer-authorize]
   [authentication.authorize.labs :as labs-authorize]
   [authentication.login.flex :as flex-login]
   [authentication.login.customizer :as customizer-login]
   [authentication.login.labs :as labs-login]
   [authentication.logout.flex :as flex-logout]
   [authentication.logout.customizer :as customizer-logout]
   [authentication.logout.labs :as labs-logout]))

(def routes 
  "Route definitions for user authentication"
  [;; Original routes
   
   
   ;; Flex routes
   {:path flex-login/login-route
    :get  flex-login/login-handler}
   {:path flex-authorize/authorize-route
    :get  flex-authorize/authorize-handler}
   {:path flex-logout/logout-route
    :get  flex-logout/logout-handler}
   
   ;; Customizer routes
   {:path customizer-login/login-route
    :get  customizer-login/login-handler}
   {:path customizer-authorize/authorize-route
    :get  customizer-authorize/authorize-handler}
   {:path customizer-logout/logout-route
    :get  customizer-logout/logout-handler}
   
   ;; Labs routes
   {:path labs-login/login-route
    :get  labs-login/login-handler}
   {:path labs-authorize/authorize-route
    :get  labs-authorize/authorize-handler}
   {:path labs-logout/logout-route
    :get  labs-logout/logout-handler}])
