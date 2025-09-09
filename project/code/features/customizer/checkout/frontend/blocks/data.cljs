(ns features.customizer.checkout.frontend.blocks.data)

(def cart-content
  {   #uuid "beba6640-1e9b-4ddb-ac10-2c2c0a45cbe2"
    {:description "This is a rim from three pieces, as the name let's you guess, and your guess is right",
     :children    {}
     :model_url   "https://bucket-production-f0da.up.railway.app:443/ironrainbow/22c9855c-85ba-42a8-b629-99c0e2209d95.glb",
     :job-id      #uuid "beba6640-1e9b-4ddb-ac10-2c2c0a45cbe2",
     :name        "Three piece rim",
     :parts       {"part-001-outer-barrel"
                   {:name "Outer Barrel"
                    :description "Main outer barrel component with custom finish"
                    :price 79.99
                    :look {:basecolor "yellow"
                           :name "Fancy Yellow"}
                    :type "component"
                    :material "Aluminum"
                    :finish "Matte Black"
                    :size "18x9.5"
                    :weight "12.5 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/outer-barrel.svg"
                    :formdata {"finish" {:value "matte-black" :price 15.00}
                               "size" {:value "18x9.5" :price 0.00}
                               "material" {:value "aluminum" :price 0.00}
                               "base-price" {:value 64.99}}}
                   
                   "part-002-inner-barrel"
                   {:name "Inner Barrel"
                    :description "Inner barrel with structural reinforcement"
                    :price 69.99
                    :type "component"
                    :look {:basecolor "blue" :name "Mighty Blue"}
                    :material "Aluminum"
                    :finish "Silver"
                    :size "18x9.5"
                    :weight "8.2 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/inner-barrel.svg"
                    :formdata {"finish" {:value "silver" :price 10.00}
                               "size" {:value "18x9.5" :price 0.00}
                               "material" {:value "aluminum" :price 0.00}
                               "base-price" {:value 59.99}}}
                   
                   "part-003-face"
                   {:name "Face Plate"
                    :description "Custom face plate with unique design pattern"
                    :price 109.99
                    :type "component"
                    :look {:basecolor "green" :name "Greeny"}
                    :material "Aluminum"
                    :finish "Brushed"
                    :size "18x9.5"
                    :weight "6.8 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/face-plate.svg"
                    :formdata {"finish" {:value "brushed" :price 25.00}
                               "design" {:value "mesh-pattern" :price 15.00}
                               "material" {:value "aluminum" :price 0.00}
                               "base-price" {:value 69.99}}}
                   
                   "part-004-hardware"
                   {:name "Hardware Kit"
                    :description "Complete hardware kit including bolts, nuts, and washers"
                    :price 24.99
                    :type "hardware"
                    :look {:basecolor "red" :name "Reddy"}
                    :material "Stainless Steel"
                    :finish "Chrome"
                    :size "M12x1.5"
                    :weight "1.2 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/hardware-kit.svg"
                    :formdata {"material" {:value "stainless-steel" :price 5.00}
                               "finish" {:value "chrome" :price 3.00}
                               "size" {:value "M12x1.5" :price 0.00}
                               "base-price" {:value 16.99}}}}
     :type        "package",
     :popular     false,
     :prefix      nil,
     :id          "5adb5c5c-458e-4106-853d-f408b6fa78e9",
     :form_id     "dc0aacf1-e75d-4fdc-9b1c-ccad0e54725c",
     "formdata"   {"look-cost" {:value 30 :price 30.00},
                   "quantity"  {:qty 4 :prefix nil :value 4 :price 0.00},
                   "assembly-fee" {:value 15.03 :price 15.03},
                   "parts-total" {:value 284.96 :price 284.96},
                   "package-total" {:value 299.99 :price 299.99}},
     :price       299.99,
     :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/45924280-9dac-4614-8012-a25e866beb96.svg",
     :category_id "f90a2d4f-8482-47cc-beb9-3a94c0e6af8d"}
   
   #uuid "aa111111-2222-3333-4444-555555555555"
   {:description "Premium one-piece rim with custom finish and advanced coating technology",
    :children    {}
    :model_url   "https://bucket-production-f0da.up.railway.app:443/ironrainbow/one-piece-rim.glb",
    :job-id      #uuid "aa111111-2222-3333-4444-555555555555",
    :name        "Premium One-Piece Rim",
    :parts       {"part-001-main-body"
                   {:name "Main Body"
                    :description "Single-piece forged aluminum rim body"
                    :price 399.99
                    :look {:basecolor "green" :name "Goblin Green"}
                    :type "component"
                    :material "Forged Aluminum"
                    :finish "Premium Black"
                    :size "19x10"
                    :weight "18.5 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/premium-body.svg"
                    :formdata {"finish" {:value "premium-black" :price 35.00}
                               "size" {:value "19x10" :price 0.00}
                               "material" {:value "forged-aluminum" :price 0.00}
                               "base-price" {:value 364.99}}}}
    :type        "package",
    :popular     true,
    :prefix      nil,
    :id          "6bcd6d6d-569f-5217-964e-g519c7gb89a0",
    :form_id     "ed1bbd02-f86e-5ged-0d2d-ddbef1f6586d",
    "formdata"   {"look-cost" {:value 50 :price 50.00},
                  "quantity"  {:qty 2 :prefix nil :value 2 :price 0.00},
                  "assembly-fee" {:value 5.02 :price 5.02},
                  "parts-total" {:value 449.97 :price 449.97},
                  "package-total" {:value 449.99 :price 449.99}},
    :price       449.99,
    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/a6df589c-2b76-4687-a58c-de109ede44a2.svg",
    :category_id "f90a2d4f-8482-47cc-beb9-3a94c0e6af8d"}
   
   #uuid "cc333333-4444-5555-6666-777777777777"
   {:description "High-performance racing rim with lightweight construction and aerodynamic design",
    :children    {}
    :model_url   "https://bucket-production-f0da.up.railway.app:443/ironrainbow/racing-rim.glb",
    :job-id      #uuid "cc333333-4444-5555-6666-777777777777",
    :name        "Racing Performance Rim",
    :parts       {"part-001-racing-body"
                   {:name "Racing Body"
                    :description "Ultra-lightweight racing rim body with aerodynamic design"
                    :price 499.99
                    :type "component"
                    :look {:basecolor "red" :name "Racing Red"}
                    :material "Carbon Fiber Reinforced Aluminum"
                    :finish "Racing Red"
                    :size "20x11"
                    :weight "15.2 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/racing-body.svg"
                    :formdata {"finish" {:value "racing-red" :price 50.00}
                               "size" {:value "20x11" :price 0.00}
                               "material" {:value "carbon-fiber-aluminum" :price 0.00}
                               "base-price" {:value 449.99}}}
                   
                   "part-002-racing-valve"
                   {:name "Racing Valve"
                    :description "High-pressure racing valve with quick-release mechanism"
                    :price 39.99
                    :type "component"
                    :look {:basecolor "blue" :name "Racing Blue"}
                    :material "Titanium"
                    :finish "Titanium"
                    :size "Racing"
                    :weight "0.2 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/racing-valve.svg"
                    :formdata {"finish" {:value "titanium" :price 0.00}
                               "material" {:value "titanium" :price 0.00}
                               "base-price" {:value 39.99}}}
                   
                   "part-003-racing-cap"
                   {:name "Racing Center Cap"
                    :description "Aerodynamic center cap with team branding"
                    :price 59.99
                    :type "component"
                    :look {:basecolor "black" :name "Carbon Black"}
                    :material "Carbon Fiber"
                    :finish "Carbon"
                    :size "Racing"
                    :weight "0.5 lbs"
                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/racing-cap.svg"
                    :formdata {"finish" {:value "carbon" :price 0.00}
                               "branding" {:value "team-branding" :price 15.00}
                               "material" {:value "carbon-fiber" :price 0.00}
                               "base-price" {:value 44.99}}}}
    :type        "package",
    :popular     true,
    :prefix      nil,
    :id          "8def8f8f-781h-7439-186g-i731e9id01c2",
    :form_id     "gf3dde24-h08g-7igf-2f4f-ffdgh3h8708f",
    "formdata"   {"look-cost" {:value 75 :price 75.00},
                  "quantity"  {:qty 4 :prefix nil :value 4 :price 0.00},
                  "assembly-fee" {:value 0.02 :price 0.02},
                  "parts-total" {:value 599.97 :price 599.97},
                  "package-total" {:value 599.99 :price 599.99}},
    :price       599.99,
    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/racing-rim.svg",
    :category_id "h12c4f6h-0604-69ee-dgd1-5c16e2g8ch1f"}})
   
   
   

  
  