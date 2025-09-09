
(ns features.customizer.data.core
  (:require
   [features.customizer.data.downhill :as downhill]))

(def one-piece-rim-form
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
    ;; :description "Size Lorem Ipsum is simply dummy text of the printing and typesetting industry."
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
   
    ;; Model swap
    ;; Texture swap
   {:id          :material
    :title       "Material"
    :value-path  [:formdata :material]
    :type        :select
    :description "The material selector allows you to choose the type of material your part is made of. Different materials require specific preparation and coating techniques to achieve the best results. By selecting the appropriate material option, such as alloy or steel, we can ensure that your part receives the optimal surface preparation and coating process. This guarantees a durable and high-quality finish that meets your expectations. Choose the material that matches your part's composition and let us provide you with exceptional powder coating services tailored to your specific needs."
    :options     [{:label "Alloy" :value 1.5
                   :modifier {:path [:parts/configurator :item :model] 
                              :value {:src "/media/models/rim.glb"
                                      :rotation [0 -2 0]}}}
                  {:label "Steel" :value 1
                   :modifier {:path  [:parts/configurator :item :model] 
                              :value {:src      "/media/models/steel_rim.glb"
                                      :rotation [0 -2 1.5]}}}]}
                  
   
   {:id          :condition
    :title        "Condition"
    :value-path  [:formdata :condition]
    ;; :condition   #(if (= "Alloy" [:material])

    :type        :select
    :description "The condition option refers to the current surface of your part before powder coating. By selecting the appropriate option, such as \"Newly Manufactured\" or \"Coated\", you help us determine the necessary surface preparation techniques for your part. If your part is newly manufactured and has no prior coating, selecting the \"Newly Manufactured\" option allows us to apply the most efficient and cost-effective preparation process. On the other hand, if your part already has a coating applied or shows signs of rust or paint, selecting the \"Coated\" option helps us assess the additional steps required to achieve the desired finish. This ensures that your part receives the proper treatment for optimal powder coating results."
    :options     [{:label "Newly manufactured" :value 1}
                  {:label "Coated"             :value 2}
                  {:label "Rusty"              :value 2}
                  {:label "Chrome plated"      :value 3}]}
   
   {:id          :diamond_cut
    :title       "Diamond cut"
    :value-path  [:formdata :diamond_cut]
    :description "Diamond cut Lorem Ipsum is simply dummy text of the printing and typesetting industry."
    :prefix      "$"
    :type        :switch
    :value       100
    :condition  "(not= 1 [:material])"}
    
   {:id         :conditional_input
    :title      "Conditional Input"
    :type       :select

    :condition  "(and (= 100 [:diamond_cut]) (not= 1 [:material]))"

    :value-path [:formdata :conditional_input]
    :options    [{:label "Green" :value "green"}
                 {:label "Blue" :value "blue"}
                 {:label "Yellow" :value "Yellow"}
                 {:label "Red" :value "Red"}
                 {:label "Clear" :value "rgba(0,0,0,0)"}]}

   {:id           :repair
    :title        "Repair"
    :type         :group

    :description  "At Iron Rainbow, we understand that the condition of your alloy rims can significantly impact your vehicle's appearance and performance. That's why we offer a range of specialized rim repair services, each designed to address specific concerns and restore your rims to their optimal state.

                  Curb Rash Repair: Choose this option if your alloy rims bear the scars of curb rash or minor scratches. Our skilled technicians will skillfully mend the damaged areas, ensuring a seamless finish that enhances the overall aesthetics of your rims.

                  Welding: Opt for welding when your alloy rims have cracks or structural damage. Our experienced welders will expertly repair and reinforce the affected areas, ensuring the structural integrity and safety of your rims.

                  Straightening: If your alloy rims have endured deformation due to accidents or impacts, straightening is the solution. Our specialized equipment and skilled technicians will restore your rims to their original form, providing a balanced and smooth ride.

                  Select the appropriate service based on your rims' condition and enjoy a comprehensive solution tailored to perfection. Iron Rainbow's rim repair services ensure your rims look great and perform optimally."
    :inputs [{:id          :curb_rash_repair
              :title       "Curb rash repair"
              :service-id   "service-curb-rash-id"
              :display-value? true
              :value       40
              :prefix      "$"
              :value-path  [:formdata :repair :curb_rash_repair]
              :type        :digit_input
              :min         {:type :num :value 0}
              :max         {:type :subscribe :value [:formdata :quantity]}}
            ;;  {:id          :chemical_stripping
            ;;   :title       "Chemical stripping"
            ;;   :value-path  [:formdata :repair :chemical_stripping]
            ;;   :type        :digit_input
            ;;   :min         {:type  :num :value 0}
            ;;   :max         {:type  :subscribe :value [:formdata :quantity]}}
             {:id          :welding
              :service-id  "service-welding-id-1"
              :title       "Welding"
              :display-value? true
              :value       60
              :prefix      "$"
              :type        :digit_input
              :value-path  [:formdata :repair :welding]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}
             {:id          :straightening
              :display-value? true
              :value       200
              :prefix      "$"
              :title       "Straightening"
              :type        :digit_input
              :value-path  [:formdata :repair :straightening]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}]}
   {:id           :tyre_services
    :title        "Tyre services"
    :icon         "icons/Technical_Support.png"
    :type         :group
    :description  "Tyre services Lorem Ipsum is simply dummy text of the printing and typesetting industry."
    :inputs [{:id          :remove_and_fitting
              :title       "Tyre removal & refitting"
              :type        :digit_input
              ;;TODO
              ;; Server side-on a price egy "hivatkozás" hogy melyik collectionben melyik item adja az árat
              ;; pl.:
              ;; :price       {:collection "service" :id "remove_and_fitting"}
              :display-value? true
              :prefix      "$"
              :value       50
              ;; :unit        {:collection "service" :id "my-service-id"}
              :value-path  [:formdata :tyre-servcice :remove_and_fitting]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}
             {:id          :fitting
              :title       "Tyre fitting"
              :display-value? true
              :value       25
              :prefix      "$"
              :type        :digit_input
              :value-path  [:formdata :tyre-servcice :fitting]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}]}])



(def three-piece-rim-form
  
  [{:id          :quantity
    :title       "Quantity"
    :type        :digit_input
    :inherit     true
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
    :inherit     true
    :value-path  [:formdata :size]
    ;; :description "Size Lorem Ipsum is simply dummy text of the printing and typesetting industry."
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

  ;;  {:id          :material
  ;;   :title       "Material"
  ;;   :value-path  [:formdata :material]
  ;;   :type        :select
  ;;   :description "The material selector allows you to choose the type of material your part is made of. Different materials require specific preparation and coating techniques to achieve the best results. By selecting the appropriate material option, such as alloy or steel, we can ensure that your part receives the optimal surface preparation and coating process. This guarantees a durable and high-quality finish that meets your expectations. Choose the material that matches your part's composition and let us provide you with exceptional powder coating services tailored to your specific needs."
  ;;   :options     [{:label "Alloy" :value 1.5}
  ;;                 {:label "Steel" :value 1}]}
   
  ;;  {:id          :condition
  ;;   :title       "Condition"
  ;;   :value-path  [:formdata :condition]
  ;;   :type        :select
  ;;   :description "The condition option refers to the current surface of your part before powder coating. By selecting the appropriate option, such as \"Newly Manufactured\" or \"Coated\", you help us determine the necessary surface preparation techniques for your part. If your part is newly manufactured and has no prior coating, selecting the \"Newly Manufactured\" option allows us to apply the most efficient and cost-effective preparation process. On the other hand, if your part already has a coating applied or shows signs of rust or paint, selecting the \"Coated\" option helps us assess the additional steps required to achieve the desired finish. This ensures that your part receives the proper treatment for optimal powder coating results."
  ;;   :options     [{:label "Newly manufactured" :value 1}
  ;;                 {:label "Coated"             :value 2}
  ;;                 {:label "Rusty"              :value 2}
  ;;                 {:label "Chrome plated"      :value 3}]}

   {:id           :repair
    :title        "Repair"
    :type         :group

    :description  "At Iron Rainbow, we understand that the condition of your alloy rims can significantly impact your vehicle's appearance and performance. That's why we offer a range of specialized rim repair services, each designed to address specific concerns and restore your rims to their optimal state.

                  Curb Rash Repair: Choose this option if your alloy rims bear the scars of curb rash or minor scratches. Our skilled technicians will skillfully mend the damaged areas, ensuring a seamless finish that enhances the overall aesthetics of your rims.

                  Welding: Opt for welding when your alloy rims have cracks or structural damage. Our experienced welders will expertly repair and reinforce the affected areas, ensuring the structural integrity and safety of your rims.

                  Straightening: If your alloy rims have endured deformation due to accidents or impacts, straightening is the solution. Our specialized equipment and skilled technicians will restore your rims to their original form, providing a balanced and smooth ride.

                  Select the appropriate service based on your rims' condition and enjoy a comprehensive solution tailored to perfection. Iron Rainbow's rim repair services ensure your rims look great and perform optimally."
    :inputs [{:id          :curb_rash_repair
              :title       "Curb rash repair"
              :service-id   "service-curb-rash-id"
              :display-value? true
              :value       40;"(40 * [:size])"
              :prefix      "$"
              :value-path  [:formdata :repair :curb_rash_repair]
              :type        :digit_input
              :min         {:type :num :value 0}
              :max         {:type :subscribe :value [:formdata :quantity]}}
             {:id          :welding
              :title       "Welding"
              :service-id  "service-welding"
              :display-value? true
              :value       60
              :prefix      "$"
              :type        :digit_input
              :value-path  [:formdata :repair :welding]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}
             {:id          :straightening
              :display-value? true
              :value       200
              :prefix      "$"
              :title       "Straightening"
              :type        :digit_input
              :value-path  [:formdata :repair :straightening]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}]}
   {:id           :tyre_services
    :title        "Tyre services"
    :icon         "icons/Technical_Support.png"
    :type         :group
    :description  "Tyre services Lorem Ipsum is simply dummy text of the printing and typesetting industry."
    :inputs [{:id          :remove_and_fitting
              :title       "Tyre removal & refitting"
              :service-id  "remove-and-fitting-service-id"
              :type        :digit_input
              :display-value? true
              :prefix      "$"
              :value       50
              :value-path  [:formdata :tyre-servcice :remove_and_fitting]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}
             {:id          :fitting
              :title       "Tyre fitting"
              :display-value? true
              :value       25
              :prefix      "$"
              :type        :digit_input
              :value-path  [:formdata :tyre-servcice :fitting]
              :min         {:type  :num :value 0}
              :max         {:type  :subscribe :value [:formdata :quantity]}}]}])

  ;;  {:id          :tyre_services
  ;;   :title       "Tyre services"
  ;;   :type        :checkbox
  ;;   :value-path  [:formdata :tyre_services]
  ;;   :description "Tyre services Lorem Ipsum is simply dummy text of the printing and typesetting industry."
  ;;   :options     [{:label "Tyre removal & refitting" :value :remove_and_fitting}
  ;;                 {:label "Tyre fitting"             :value :fitting}]}])

(def three-piece-rim-part-form
  
  [
   {:id          :material
    :title       "Material"
    :value-path  [:formdata :material]
    :type        :select
    :description "The material selector allows you to choose the type of material your part is made of. Different materials require specific preparation and coating techniques to achieve the best results. By selecting the appropriate material option, such as alloy or steel, we can ensure that your part receives the optimal surface preparation and coating process. This guarantees a durable and high-quality finish that meets your expectations. Choose the material that matches your part's composition and let us provide you with exceptional powder coating services tailored to your specific needs."
    :options     [{:label "Alloy" :value 1.5}
                  {:label "Steel" :value 1}]}
   
   {:id          :condition
    :title       "Condition"
    :value-path  [:formdata :condition]
    :type        :select
    :description "The condition option refers to the current surface of your part before powder coating. By selecting the appropriate option, such as \"Newly Manufactured\" or \"Coated\", you help us determine the necessary surface preparation techniques for your part. If your part is newly manufactured and has no prior coating, selecting the \"Newly Manufactured\" option allows us to apply the most efficient and cost-effective preparation process. On the other hand, if your part already has a coating applied or shows signs of rust or paint, selecting the \"Coated\" option helps us assess the additional steps required to achieve the desired finish. This ensures that your part receives the proper treatment for optimal powder coating results."
    :options     [{:label "Newly manufactured" :value 1}
                  {:label "Coated"             :value 2}
                  {:label "Rusty"              :value 2}
                  {:label "Chrome plated"      :value 3}]}])

(def FORMS 
  {:one-piece-rim-form        one-piece-rim-form
   :three-piece-rim-item-form three-piece-rim-form
   :three-piece-rim-face      three-piece-rim-part-form
   :three-piece-rim-barrel    three-piece-rim-part-form
   :three-piece-rim-lip       three-piece-rim-part-form
   :downhill-basic-template   downhill/basic-template})

(def three-piece-rim-price-formula
  "([:diamond_cut] + [:repair :curb_rash_repair] + [:repair :welding] + [:repair :straightening] + [:tyre-servcice :remove_and_fitting] + [:tyre-servcice :fitting])")

(def three-piece-rim-part-price-formula
  "([:quantity] * 
    ([:look-cost] * [:size] * [:material] * [:condition]))")

(def this-parts 
  {"rim" {:id             "rim"
          :name           "One piece rim"
          :icon           "icons/alloy-wheel.svg"
          :description     "<div>
                            <b style=\"color:var(--irb-clr)\">Alloy wheel</b>
                            Crafted from a single piece of metal, these rims offer simplicity and versatility in customization. Choose a single color or finish, and watch as the entire rim receives a uniform and durable coating through our meticulous process.

                            Our powder coating journey for one-piece rims involves comprehensive cleaning, precision sandblasting, and expert coating. The result is not just a finish but an enhancement that speaks volumes about your style and durability expectations. Elevate your ride with the precision and quality of Iron Rainbow's powder coating expertise.

                            <b style=\"color:var(--irb-clr)\">Steel rim</b>
                            Steel rims are made up of a single piece of metal and are the simplest type of rims to powder coat. These rims can be powder coated in a single color or finish, and the entire rim will be coated uniformly. Our powder coating process for one piece steel rims includes thorough cleaning, sandblasting, and coating to ensure that the finish is uniform and durable.

                            For those with steel rims, powder coating can also provide an added layer of protection against corrosion. This is especially important for winter tires, which are often fitted onto steel rims and exposed to harsh environments during the winter months. Our high-quality powder coating can help protect your steel rims from corrosion while also giving them a beautiful, eye-catching finish.
                            </div>"
          :type           "part"
          :model          {:src       "/media/models/rim.glb"
                           :rotation [0 -2 0]}
          :price-group     {:basic  70
                            :basic+ 90
                            :pro    120
                            :pro+   140}
          :look-allowed   []
          :form           {:id       :one-piece-rim-form
                           :price-formula three-piece-rim-part-price-formula
                           :template one-piece-rim-form
                           :metadata {:timestamp "form-timestamp"}}}

   "three-piece-rim" {:id            "three-piece-rim"
                      :name          "Three piece rim"
                      :icon          "icons/3-piece-rim.svg"
                      :description   "Three piece rim bio"
                      :type          "package"
                      :parts         ["barrel" "face" "lip"]
                      :model         {:src      "/media/models/three_piece_rim.gltf" 
                                      :rotation [0 -0.8 0]}
                      :look-allowed  []
                      :form          {:id            :three-piece-rim-form
                                      :template      three-piece-rim-form
                                      :price-formula three-piece-rim-price-formula
                                      :metadata      {:timestamp "form-timestamp"}}}
   
   "barrel"        {:id            "barrel"
                    :name          "Barrel"
                    :icon          "icons/3-piece-barrel.svg"
                    :description   "Three piece rim bio"
                    :type          "part"
                    :package-id    "three-piece-rim"
                    :look-allowed  []
                    :model         {:src "/media/models/three_piece_rim.gltf" 
                                    :rotation [1 -0.8 0]}
                    :price-group   {:basic  70
                                    :basic+ 90
                                    :pro    110
                                    :pro+   130}
                    :form          {:id            :three-piece-rim-barrel
                                    :template      three-piece-rim-part-form
                                    :price-formula three-piece-rim-part-price-formula
                                    :metadata      {:timestamp "form-timestamp"}}}

   "face"          {:id            "face"
                    :name          "Face"
                    :icon          "icons/face.svg"
                    :description   "Three piece rim Face bio"
                    :type          "part"
                    :package-id    "three-piece-rim"
                    :model         {:src      "/media/models/three_piece_rim.gltf" 
                                    :rotation [0 -0.8 2]}
                    :look-allowed  []
                    :price-group   {:basic  70
                                    :basic+ 90
                                    :pro    120
                                    :pro+   140}
                    :form          {:id            :three-piece-rim-face
                                    :template      three-piece-rim-part-form
                                    :price-formula three-piece-rim-part-price-formula
                                    :metadata      {:timestamp "form-timestamp"}}}
   
   "lip"          {:id             "lip"
                   :name           "Lip"
                   :icon           "icons/lip.svg"
                   :description    "Three piece rim Lip bio"
                   :type           "part"
                   :package-id     "three-piece-rim"
                   :model         {:src      "/media/models/three_piece_rim.gltf" 
                                   :rotation [0 1 0]}
                   :look-allowed   []
                   :price-group    {:basic  40
                                    :basic+ 90
                                    :pro    120
                                    :pro+   140}
                   :form           {:id            :three-piece-rim-lip
                                    :template      three-piece-rim-part-form
                                    :price-formula three-piece-rim-part-price-formula
                                    :metadata      {:timestamp "form-timestamp"}}}
   
   "downhill" {:id             "downhill"
               :name           "Downhill"
               :icon           "icons/bicycle.svg"
               :description    "downhill bio"
               :type           "package"
               :model          {:src       "/media/models/downhill_2.glb"
                                :rotation [0 2.5 0]}
               :parts          ["barrel" "face" "lip"]
               :look-allowed   []
               :form           {:id       :three-piece-rim-item-form    ;; Az item betöltött 
                                :template three-piece-rim-form
                                :metadata {:timestamp "form-timestamp"}}}
   
   "car"             {:id             "car"
                      :name           "Car"
                      :grouped? true
                      :model          {:src       "/media/models/car.glb"
                                       :rotation [0 -0.8 0]}
                      :description    ""
                      :look-allowed []}




   "drum"             {:id             "drum"
                       :name           "Drum"
                       :model          {:src       "/media/models/model11.glb"
                                        :rotation [0 -0.8 0]}
                       :description    ""
                       :look-allowed []}
   

   "truck-rim"     {:id             "truck-rim"
                    :name           "Rim"
                    :icon           "icons/alloy-wheel.svg"
                                   ;; model object contains all sort of configuration for model appeareance
                    :model          {:src       "/media/models/rim1.glb"
                                     :rotation [(/ js/Math.PI 2) 0 0.8]}
                    
                    :description    "I know it's not a truck rim, but now it is."
                    :price-group        {:basic      130
                                         :basic+ 160
                                         :pro        220
                                         :pro+   340}
                    :look-allowed []
                    :form           {:id       :one-piece-rim--form-snapshot-1    ;; Az item betöltött 
                                     :template one-piece-rim-form
                                     :metadata {:timestamp "form-timestamp"}}
                    :form-template  one-piece-rim-form}

   "guitar"   {:id             "guitar"
               :name           "Guitar"
               :icon           "icons/bicycle.svg"
                                 ;; model object contains all sort of configuration for model appeareance
               :model          {:src       "/media/models/model6.glb"
                                :rotation [1 0 0]}
               :description    ""
               :look-allowed []
               :grouped?       true}

   "1" {:id             "1"
        :name           "Guitar piece"
        :icon           "icons/bicycle.svg"
        :description    ""
        :price-group         {:basic  60
                              :basic+ 80
                              :pro    100
                              :pro+   120}
        :look-allowed []
        :form-template  downhill/basic-template}})       

(def parts
  (merge this-parts
         downhill/parts))
                                

(def USA-STATES
  {
    "Alabama" "AL",
    "Alaska" "AK",
    "Arizona" "AZ",
    "Arkansas" "AR",
    "California" "CA",
    "Colorado" "CO",
    "Connecticut" "CT",
    "Delaware" "DE",
    "Florida" "FL",
    "Georgia" "GA",
    "Hawaii" "HI",
    "Idaho" "ID",
    "Illinois" "IL",
    "Indiana" "IN",
    "Iowa" "IA",
    "Kansas" "KS",
    "Kentucky" "KY",
    "Louisiana" "LA",
    "Maine" "ME",
    "Maryland" "MD",
    "Massachusetts" "MA",
    "Michigan" "MI",
    "Minnesota" "MN",
    "Mississippi" "MS",
    "Missouri" "MO",
    "Montana" "MT",
    "Nebraska" "NE",
    "Nevada" "NV",
    "New Hampshire" "NH",
    "New Jersey" "NJ",
    "New Mexico" "NM",
    "New York" "NY",
    "North Carolina" "NC",
    "North Dakota" "ND",
    "Ohio" "OH",
    "Oklahoma" "OK",
    "Oregon" "OR",
    "Pennsylvania" "PA",
    "Rhode Island" "RI",
    "South Carolina" "SC",
    "South Dakota" "SD",
    "Tennessee" "TN",
    "Texas" "TX",
    "Utah" "UT",
    "Vermont" "VT",
    "Virginia" "VA",
    "Washington" "WA",
    "West Virginia" "WV",
    "Wisconsin" "WI",
    "Wyoming" "WY"})


        
;; TODO
;; Get services with form
;; Form should contains :services field what contains all the service-id, what is used by the template
;; The inputs what contains service-id, should give the value from the service 
;; Services should use price-formula
