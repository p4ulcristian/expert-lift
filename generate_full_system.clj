(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn extract-event-name [line]
  "Extract event name from a re-frame registration line"
  (when-let [match (re-find #"[\s(]+(:[^\s\]]+)" line)]
    (second match)))

(defn extract-subscription-name [line]
  "Extract subscription name from reg-sub line"
  (when-let [match (re-find #"[\s(]+(:[^\s\]]+)" line)]
    (second match)))

(defn extract-dispatch-target [line]
  "Extract dispatch target from dispatch line"
  (when-let [match (re-find #"\[[\s]*(:[^\s\]]+)" line)]
    (second match)))

(defn analyze-full-system []
  (println "🚀 Analyzing COMPLETE Re-frame System...")
  
  (let [files (->> (file-seq (io/file "./project/code"))
                   (filter #(.isFile %))
                   (filter #(or (.endsWith (.getName %) ".cljs")
                               (.endsWith (.getName %) ".clj")))
                   (map #(.getPath %)))
        
        all-events (atom [])
        all-subs (atom [])
        all-dispatches (atom [])
        all-resolvers (atom [])]
    
    (doseq [file files]
      (try
        (let [lines (str/split-lines (slurp file))
              relative-file (str/replace file "./project/code/" "")]
          (doseq [[line-num line] (map-indexed vector lines)]
            ;; Extract events
            (when (re-find #"r/reg-event-|re-frame\.core/reg-event-" line)
              (when-let [event-name (extract-event-name line)]
                (swap! all-events conj {:name event-name 
                                       :file relative-file 
                                       :line (inc line-num)})))
            
            ;; Extract subscriptions
            (when (re-find #"r/reg-sub|re-frame\.core/reg-sub" line)
              (when-let [sub-name (extract-subscription-name line)]
                (swap! all-subs conj {:name sub-name 
                                     :file relative-file 
                                     :line (inc line-num)})))
            
            ;; Extract dispatches  
            (when (re-find #"r/dispatch|re-frame\.core/dispatch" line)
              (when-let [target (extract-dispatch-target line)]
                (swap! all-dispatches conj {:target target 
                                           :file relative-file 
                                           :line (inc line-num)})))
            
            ;; Extract pathom resolvers
            (when (re-find #"pco/defresolver" line)
              (when-let [resolver-name (second (re-find #"defresolver\s+([^\s\[]+)" line))]
                (swap! all-resolvers conj {:name resolver-name 
                                          :file relative-file 
                                          :line (inc line-num)})))))
        (catch Exception e
          (println "Error processing file:" file))))
    
    (let [events @all-events
          subs @all-subs  
          dispatches @all-dispatches
          resolvers @all-resolvers]
      
      (println (str "\n📊 COMPLETE SYSTEM ANALYSIS:"))
      (println (str "  🔵 Events: " (count events)))
      (println (str "  🟢 Subscriptions: " (count subs)))
      (println (str "  ⚡ Dispatches: " (count dispatches)))
      (println (str "  🟣 Pathom Resolvers: " (count resolvers)))
      
      ;; Create comprehensive JSON for visualization
      (let [all-nodes (concat
                       (map-indexed 
                        (fn [idx event]
                          {:id (inc idx)
                           :name (:name event)
                           :type "event" 
                           :file (:file event)
                           :line (:line event)}) 
                        events)
                       
                       (map-indexed 
                        (fn [idx sub]
                          {:id (+ (count events) idx 1)
                           :name (:name sub)
                           :type "subscription"
                           :file (:file sub)
                           :line (:line sub)}) 
                        subs)
                       
                       (map-indexed 
                        (fn [idx resolver]
                          {:id (+ (count events) (count subs) idx 1)
                           :name (:name resolver)
                           :type "pathom-resolver"
                           :file (:file resolver)
                           :line (:line resolver)}) 
                        resolvers))
            
            ;; Create links based on dispatch relationships
            event-names (set (map :name events))
            links (for [dispatch dispatches
                       :when (event-names (:target dispatch))
                       :let [source-event (first (filter #(= (:file %) (:file dispatch)) events))
                             target-event (first (filter #(= (:name %) (:target dispatch)) events))]
                       :when (and source-event target-event)]
                   {:source (inc (.indexOf events source-event))
                    :target (inc (.indexOf events target-event))
                    :type "dispatch"})
            
            full-data {:nodes all-nodes
                      :links links
                      :metadata {:total-events (count events)
                                :total-subs (count subs)
                                :total-dispatches (count dispatches)
                                :total-resolvers (count resolvers)
                                :timestamp (str (java.time.Instant/now))}}]
        
        ;; Write the complete system data
        (io/make-parents "tools/output/full-system.json")
        (spit "tools/output/full-system.json" 
              (str "window.fullSystemData = " (pr-str full-data) ";"))
        
        (println "\n✅ COMPLETE SYSTEM DATA GENERATED!")
        (println "📄 File: tools/output/full-system.json")
        (println (str "📊 Total nodes: " (count all-nodes)))
        (println (str "🔗 Total links: " (count links)))
        
        ;; Show sample of what was found
        (println "\n🎯 Sample Events:")
        (doseq [event (take 10 events)]
          (println (str "  " (:name event) " (" (:file event) ":" (:line event) ")")))
        
        (println "\n🎯 Sample Subscriptions:")
        (doseq [sub (take 5 subs)]
          (println (str "  " (:name sub) " (" (:file sub) ":" (:line sub) ")")))))))

;; Run the complete analysis
(analyze-full-system)