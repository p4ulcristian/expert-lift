(ns features.flex.locations.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-locations
  "Get locations data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:locations/get-locations {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [locations (:locations/get-locations response)]
                  (callback locations)))}))

(defn get-location
  "Get single location using ParQuery"
  [workspace-id location-id callback]
  (parquery/send-queries
   {:queries {:locations/get-location {:workspace-id workspace-id :location-id location-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [location (:locations/get-location response)]
                  (callback location)))}))

(defn create-location
  "Create new location using ParQuery"
  [workspace-id location-data callback]
  (parquery/send-queries
   {:queries {:locations/create-location (merge {:workspace-id workspace-id} location-data)}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:locations/create-location response)]
                  (callback result)))}))

(defn edit-location
  "Edit location using ParQuery"
  [workspace-id location-data callback]
  (parquery/send-queries
   {:queries {:locations/edit-location (merge {:workspace-id workspace-id} location-data)}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:locations/edit-location response)]
                  (callback result)))}))

(defn delete-location
  "Delete location using ParQuery"
  [workspace-id location-id callback]
  (parquery/send-queries
   {:queries {:locations/delete-location {:workspace-id workspace-id :location-id location-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:locations/delete-location response)]
                  (callback result)))}))