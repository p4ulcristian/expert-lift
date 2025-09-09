(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn read-clojure-file [file-path]
  (when (.exists (io/file file-path))
    {:file file-path
     :content (slurp file-path)}))

(defn extract-patterns [content]
  (let [lines (str/split-lines content)
        results {:events [] :subs [] :dispatches [] :functions []}]
    
    (reduce (fn [acc [line-num line]]
              (cond
                ;; Find re-frame events
                (re-find #"r/reg-event-|re-frame\.core/reg-event-" line)
                (if-let [event-name (second (re-find #"[\s(]+(:\S+)" line))]
                  (update acc :events conj {:name event-name :line (inc line-num) :file (:current-file acc)})
                  acc)
                
                ;; Find subscriptions  
                (re-find #"r/reg-sub|re-frame\.core/reg-sub" line)
                (if-let [sub-name (second (re-find #"[\s(]+(:\S+)" line))]
                  (update acc :subs conj {:name sub-name :line (inc line-num) :file (:current-file acc)})
                  acc)
                
                ;; Find dispatches
                (re-find #"r/dispatch|re-frame\.core/dispatch" line)
                (if-let [dispatch-target (second (re-find #"\[[\s]*(:\S+)" line))]
                  (update acc :dispatches conj {:target dispatch-target :line (inc line-num) :file (:current-file acc)})
                  acc)
                
                ;; Find function definitions
                (re-find #"^\s*\(defn\s+" line)
                (if-let [fn-name (second (re-find #"defn\s+([^\s\[]+)" line))]
                  (update acc :functions conj {:name fn-name :line (inc line-num) :file (:current-file acc)})
                  acc)
                
                :else acc))
            (assoc results :current-file nil)
            (map-indexed vector lines))))

(defn analyze-file [file-path]
  (when-let [parsed (read-clojure-file file-path)]
    (let [patterns (extract-patterns (:content parsed))]
      (-> patterns
          (assoc :file file-path)
          (dissoc :current-file)
          (update :events #(map (fn [e] (assoc e :file file-path)) %))
          (update :subs #(map (fn [e] (assoc e :file file-path)) %))
          (update :dispatches #(map (fn [e] (assoc e :file file-path)) %))
          (update :functions #(map (fn [e] (assoc e :file file-path)) %))))))

(defn find-files [dir extensions]
  (->> (file-seq (io/file dir))
       (filter #(.isFile %))
       (filter #(some (fn [ext] (.endsWith (.getName %) ext)) extensions))
       (map #(.getPath %))))

(defn analyze-project []
  (println "🚀 Analyzing Clojure/Re-frame Call Tree...")
  
  (let [files (find-files "./project/code" [".clj" ".cljs"])
        _ (println (str "📁 Found " (count files) " files to analyze"))
        analysis-results (->> files
                             (map analyze-file)
                             (filter some?))
        all-events (mapcat :events analysis-results)
        all-subs (mapcat :subs analysis-results) 
        all-dispatches (mapcat :dispatches analysis-results)
        all-functions (mapcat :functions analysis-results)]
    
    (println "\n📊 ANALYSIS RESULTS:")
    (println (str "  🔵 Events found: " (count all-events)))
    (println (str "  🟢 Subscriptions found: " (count all-subs)))
    (println (str "  ⚡ Dispatches found: " (count all-dispatches)))
    (println (str "  🟠 Functions found: " (count all-functions)))
    
    (println "\n🎯 Sample Events Found:")
    (doseq [event (take 10 all-events)]
      (println (str "  " (:name event) " (" (last (str/split (:file event) #"/")) ":" (:line event) ")")))
    
    (println "\n🎯 Sample Subscriptions Found:")
    (doseq [sub (take 10 all-subs)]
      (println (str "  " (:name sub) " (" (last (str/split (:file sub) #"/")) ":" (:line sub) ")")))
    
    (println "\n🎯 Sample Dispatches Found:")
    (doseq [dispatch (take 10 all-dispatches)]
      (println (str "  " (:target dispatch) " (" (last (str/split (:file dispatch) #"/")) ":" (:line dispatch) ")")))
    
    (println "\n🎯 Sample Functions Found:")
    (doseq [fn (take 10 all-functions)]
      (println (str "  " (:name fn) " (" (last (str/split (:file fn) #"/")) ":" (:line fn) ")")))
    
    ;; Create simple JSON-like output for visualization
    (io/make-parents "tools/output/analysis.edn")
    (let [viz-data {:nodes (concat
                           (map-indexed #(hash-map :id (inc %1) :name (:name %2) :type "event" :file (:file %2)) (take 20 all-events))
                           (map-indexed #(hash-map :id (+ 21 %1) :name (:name %2) :type "subscription" :file (:file %2)) (take 20 all-subs))
                           (map-indexed #(hash-map :id (+ 41 %1) :name (:name %2) :type "function" :file (:file %2)) (take 20 all-functions)))
                    :links (map-indexed #(hash-map :source (inc %1) :target (+ %1 21) :type "dispatch") (range 10))
                    :metadata {:total-events (count all-events)
                              :total-subs (count all-subs)
                              :total-dispatches (count all-dispatches)
                              :total-functions (count all-functions)
                              :files-analyzed (count analysis-results)}}]
      (spit "tools/output/analysis.edn" (pr-str viz-data))
      (println "\n✅ Analysis complete!")
      (println "📄 Results saved to tools/output/analysis.edn")
      (println "🌐 You can now view the results in the visualization"))))

;; Run the analysis
(analyze-project)