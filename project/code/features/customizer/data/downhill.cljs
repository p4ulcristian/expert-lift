(ns features.customizer.data.downhill)


(def basic-template
  [{:id          :quantity
    :title       "Quantity"
    :type        :digit_input
    :value       1
    :value-path  [:formdata :quantity]
    :min         {:type :num :value 1}
    :max         {:type :num :value 99}
    :description "The quantity of parts being coated may affect the pricing, as bulk orders often offer economies of scale and cost savings compared to smaller quantities.

                  It's important to note that the pricing for your specific coating job will be determined based on these factors and any additional services or requirements you select during the customization process. Our pricing structure is designed to be transparent, providing you with a fair and competitive quote based on your unique needs and specifications.

                  At Iron Rainbow, we strive to offer quality coatings at competitive prices, ensuring you receive the best value for your investment. Our team is dedicated to providing accurate and transparent pricing, so you can make informed decisions and achieve the desired results within your budget."}
   {:id          :size
    :title       "Size"
    :type        :select
    :value-path  [:formdata :size]
    :options     [{:label "13”", :value 1}
                  {:label "14”", :value 1.1}
                  {:label "15”", :value 1.2}
                  {:label "16”", :value 1.3}
                  {:label "17”", :value 1.4}
                  {:label "18”", :value 1.5}
                  {:label "19”", :value 1.6}
                  {:label "20”", :value 1.7}
                  {:label "21”", :value 1.8}
                  {:label "22”", :value 1.9}]}

   {:id          :material
    :title       "Material"
    :value-path  [:formdata :material]
    :type        :select
    :description "The material selector allows you to choose the type of material your part is made of. Different materials require specific preparation and coating techniques to achieve the best results. By selecting the appropriate material option, such as alloy or steel, we can ensure that your part receives the optimal surface preparation and coating process. This guarantees a durable and high-quality finish that meets your expectations. Choose the material that matches your part's composition and let us provide you with exceptional powder coating services tailored to your specific needs."
    :options     [{:label "Alloy"  :value 1.5}
                  {:label "Steel"  :value 1}
                  {:label "Carbon" :value 2}]}
   
   {:id          :condition
    :title        "Condition"
    :value-path  [:formdata :condition]
    :type        :select
    :description "The condition option refers to the current surface of your part before powder coating. By selecting the appropriate option, such as \"Newly Manufactured\" or \"Coated\", you help us determine the necessary surface preparation techniques for your part. If your part is newly manufactured and has no prior coating, selecting the \"Newly Manufactured\" option allows us to apply the most efficient and cost-effective preparation process. On the other hand, if your part already has a coating applied or shows signs of rust or paint, selecting the \"Coated\" option helps us assess the additional steps required to achieve the desired finish. This ensures that your part receives the proper treatment for optimal powder coating results."
    :options     [{:label "Newly manufactured" :value 1}
                  {:label "Coated"             :value 2}
                  {:label "Rusty"              :value 2}
                  {:label "Chrome plated"      :value 3}]}

   {:id           :repair
    :title        "Repair"
    :type         :group
    :inputs [{:id             :repair
              :title          "Repair"
              :service-id     "downhill_part_repair"
              :value          40
              :prefix         "$"
              :value-path     [:formdata :repair :curb_rash_repair]
              :type           :digit_input
              :min            {:type :num :value 0}
              :max            {:type :subscribe :value [:formdata :quantity]}}
             {:id             :welding
              :service-id  "service-welding-id"
              :title       "Welding"
              :display-value? true
              :value       60
              :prefix      "$"
              :type        :digit_input
              :value-path  [:formdata :repair :welding]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}]}])
   
(defn part-structure [id name icon]
  {:id             id
   :name           name
   :icon           icon
   :type           "part"
   :description    ""
   :package-id    "downhill"
   :price-group    {:basic  70
                    :basic+ 90
                    :pro    120
                    :pro+   140}
   :model          {:src       "/media/models/downhill_2.glb"}
   :colors-allowed []
   :form           {:id       :downhill-basic-template
                    :template basic-template
                    :metadata {:timestamp "form-timestamp"}}})
(def parts 
  {
   "spokes"       (part-structure "spokes" "Spokes" "icons/bicycle/spokes.svg")
   "handlebars"   (part-structure "handlebars" "Handlebars" "icons/bicycle/handlebars.svg")
;; "Plane012_2"
   "brake-lever"  (part-structure "brake-lever" "Brake Lever" "icons/bicycle/brake-lever.svg")
;; "reservoir"
;; "28"
;; "front-brake-cable"
   "hub"           (part-structure "hub" "Hub" "icons/bicycle/hub.svg")
   "rear-suspension-linkage" (part-structure "rear-suspension-linkage" "Rear Suspension Linkage" "icons/bicycle/rear-suspension-linkage.svg")
   "brake-caliper"  (part-structure "brake-caliper" "Brake Caliper" "icons/bicycle/brake-caliper.svg")
  ;;  "brake-disc" ()
;; "rear-cables"
;; "tire1"
   "pedal"       (part-structure "pedal" "Pedal" "icons/bicycle/pedal.svg")
  ;;  "47"
  ;;  "Circle012_1"
   "crown"       (part-structure "crown" "Crown" "icons/bicycle/crown.svg")
;; "Plane012"
;; "spokes-1"
;; "casette"
   "rim-rear"   (part-structure "rim-rear" "Rim Rear" "icons/bicycle/wheel-rim.svg")
;; "shaft"
;; "42"
   "coil-spring" (part-structure "coil-spring" "Coil Spring" "icons/bicycle/coil-spring.svg")
;; "hub-1"
   "swing-arm"  (part-structure "swing-arm" "Swing Arm" "icons/bicycle/swing-arm.svg")
;; "24"
;; "Plane012_1"
   "lowers"     (part-structure "lowers" "Lowers" "icons/bicycle/lowers.svg")
;; "Circle029"
;; "38"
   "rim-front"  (part-structure "rim-front" "Rim Front" "icons/bicycle/wheel-rim.svg")
;; "brake-disc-1"
;; "tire"
   "saddle-rail"  (part-structure "saddle-rail" "Saddle Rail" "icons/bicycle/saddle-rail.svg")
;; "Scene"
;; "Circle029_1"
   "seat-post" (part-structure "seat-post" "Seat Post" "icons/bicycle/seat-post.svg")
   "crankset" (part-structure "crankset" "Crankset" "icons/bicycle/crankset.svg")
   "frame" (part-structure "frame" "Frame" "icons/bicycle/frame.svg")
;; "brake-caliper"
   "seat-post-clamp" (part-structure "seat-post-clamp" "Seat Post Clamp" "icons/bicycle/seat-post-clamp.svg")
   "chainring" (part-structure "chainring" "Chainring" "icons/bicycle/chainring.svg")
;; "Circle012"
;; "grips"
;; "seat"
;; "chain"
   "rear-derailleur" (part-structure "rear-derailleur" "Rear Derailleur" "icons/bicycle/rear-derailleur.svg")
;; "monoblock"
   "stem"       (part-structure "stem" "Stem" "icons/bicycle/stem.svg")})
;; "brake-brake"
;; "pedal-1"