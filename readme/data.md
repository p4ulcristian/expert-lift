
# CATEGORY PROTOTYPE

{
 :id          (string || ObjectID) 
 :name        (string)
 :children    (vector of category object)
 :part?       (boolean)
 :background  (storage/uri)
 :thumbnail   (storage/uri)
 :description (string)
}

# CATEGORIES EXAMPLE

["wheel" "three-piece-rim" "berrel"]
["Vehicles" :chilren 0]

{"wheel"
  {"three-piece-rim"
   {:metadata {:name "noasdf"}
    "barrel" {:metadata {:name "bas"}}
    "face" {:metadata {:name "bas"}}
    "bolts" {:metadata {:name "bas"}}}}}
  

 [{:name      "Vehicles"
   :children 
    [{:name     "Car"
      :children [{:id "rim" 
                  :name "Rim" 
                  :children [{:id    "one-piece-rim" 
                              :name  "One piece" 
                              :part? true}
                             {:id       "multi-piece"
                              :name     "Multi piece"
                              :children [{:id "center"    :name "Center"    :part? true}
                                         {:id "outer-rim" :name "Outer rim" :part? true}
                                         {:id "inner-rim" :name "Inner rim" :part? true}]}]}
                 {:name "Brake caliper"}
                 {:name "Shock spring"}
                 {:name "Wiper"}
                 {:name "Hood hinges"}
                 {:name "Trailer hitch"}
                 {:name "Hitch receiver"}]}
     {:name "Bicycle"}
     {:name "Motorcycle"
      :children [{:id "rim" 
                  :name "Rim" 
                  :children [{:id    "one-piece-rim" 
                              :name  "One piece"
                              :part? true}
                             {:id       "multi-piece"
                              inpu:name     "Multi piece"
                              :children [{:id "center"    :name "Center"    :part? true}
                                         {:id "outer-rim" :name "Outer rim" :part? true}
                                         {:id "inner-rim" :name "Inner rim" :part? true}]}]}]}
     {:name "Truck"}
     {:name "Scooter"}
     {:name "Boat"}
     {:name "Aircraft"}
     {:name "Semi truck"}
     {:name     "Snowmobile"
      :children [{:name "Slide rail"}
                 {:name "Idler bracket"}
                 {:name "Throttle"}
                 {:name "Handlebar"}
                 {:name "Brake lever"}]}

     {:name "Trailer"}
     {:name "RV"}
     {:name "Bus"}
     {:name "Trike"}
     {:name     "Agriculture"
      :children [{:name "Tractor"}
                 {:name     "Sit on mower"
                  :children [{:name "Rim"}
                             {:name "Blade"}
                             {:name "Hood"}
                             {:name "Drive pulley"}
                             {:name "Deck"}]}]}

     {:name     "Other"
      :children [{:name "Monocycle"}
                 {:name "Hoverboard"}]}


     {:name     "Accessories"
      :children [{:name "Shackle"}
                 {:name "Car jack"}
                 {:name "Cross bars"}
                 {:name "Bike rack"}
                 {:name "Tow hook"}]}]}
  
  {:name     "Home"
   :children []}
  {:name     "Garden"
   :children []}
  {:name     "Sports"
   :children []}
  {:name     "Workshop"
   :children []}
  {:name     "Instruments"
   :children []}])

# PART PROTOTYPE

{:id             (string || ObjectID)
 :name           (string)
 :weight         (int)
 :operating-temp (int)
 :icon           {:storage/id "icon-id" :storage/uri "icon-uri"}
 :price          (int)
 :price-calculation  
              (fn [price]
                    {:basic      price
                     :basic-plus (* 1.2 price)
                     :pro        (* 1.5 price)
                     :pro-plus   (* 1.8 price)}
 :dimensions     {:height (int)
                  :width  (int)
                  :length (int)}
 :config         {:froms/id "one-piece-rim-form-id"}}
 :document       {:documents/id "one-piece-rim-document"}
 :entries        {:color-exceptions [{:color/id "color-id-1"} {:color/id "color-id-2"}]
                  :food-ready? (boolean)}

# INPUT PROTOTYPE

{:id          (string || ObjectID)
 :name        (string)
 :type        (keyword)
 :description (string)
 :disable?    (function)
 :value-path  (re-frame-db-path)
}

# FORM PROTOTYPE

 {:id       :type
  :name     "Type"
  :type     :select
  :options  [:oven :sndblaster :asd]}

 {:id         :quantity
  :name       "Quantity"
  :type       :digit_input
  :min        {:type :num :value 1}
  :max        {:type :num :value 99}
  :value-path [:parts/configurator :formdata :quantity]}
 {:id       :size
  :name     "Size"
  :type     :select
  :options  [13 14 15 16 17 18 19 20 21 22]}
 {:id        :material 
  :name      "Material"
  :type      :radio_button_group
  :options   ["Alloy" "Steel"]
  :description ""}
 {:id        :condition
  :name      "Condition"
  :condition #(= % 1)
  :type      :radio_button_group
  :options   ["Newly manufactured" "Coated" "Rusty" "Chrome plated"]}
{:id     :repair
:name   "Repair"
:type   :group
:inputs [{:id   :curb_rash_repair
       :name "Curb rash repair"
       :type :digit_input
       :min  {:type  :num 
              :value 0}
       :max  {:type  :subscribe 
              :value [:formdata :quantity]}} 
       {:id   :welding
       :name "Welding" 
       :type :digit_input
       :min  {:type  :num 
              :value 0}
       :max  {:type  :subscribe 
              :value [:formdata :quantity]}} 
       {:id   :straightening
       :name "Straightening"
       :type :digit_input
       :min  {:type  :num 
              :value 0}
       :max  {:type  :subscribe 
              :value [:formdata :quantity]}}]}
{:id   "diamond_cut"
:name "Diamond cut"
:type :radio_button}
{:id        "tyre_services"
:name      "Tyre services"
:condition #(= % 1)
:type      :checkbox_group
:options   ["Tyre removal & refitting" "Tyre fitting"]}

# Document PROTOTYPE

{:id                    "one-piece-rim-document"
 :short-description     (string)
 :customer-instructions (string)
 :coating-instructions  (string)}

# PARTS

 {}

 {
  "one-piece-rim" {:id             "one-piece-rim"
                   :name           "Rim"
                   :icon           ""
                   :description    ""
                   :price-group   {:basic      60
                                   :basic-plus 80
                                   :pro        100
                                   :pro-plus   120}
                   :colors-allowed []
                   :formdata       [{:id         :quantity
                                     :name       "Quantity"
                                     :type       :digit_input
                                     :min        {:type :num :value 1}
                                     :max        {:type :num :value 99}
                                     :value-path [:parts/configurator :formdata :quantity]}
                                    {:id       :size
                                     :name     "Size"
                                     :type     :select
                                     :options  [13 14 15 16 17 18 19 20 21 22]}
                                    {:id        :material 
                                     :name      "Material"
                                     :type      :radio_button_group
                                     :options   ["Alloy" "Steel"]
                                     :description ""}
                                    {:id        :condition
                                     :name      "Condition"
                                     :condition #(= % 1)
                                     :type      :radio_button_group
                                     :options   ["Newly manufactured" "Coated" "Rusty" "Chrome plated"]}
                                    {:id     :repair
                                     :name   "Repair"
                                     :type   :group
                                     :inputs [{:id   :curb_rash_repair
                                               :name "Curb rash repair"
                                               :type :digit_input
                                               :min  {:type  :num 
                                                      :value 0}
                                               :max  {:type  :subscribe 
                                                      :value [:formdata :quantity]}} 
                                              {:id   :welding
                                               :name "Welding" 
                                               :type :digit_input
                                               :min  {:type  :num 
                                                      :value 0}
                                               :max  {:type  :subscribe 
                                                      :value [:formdata :quantity]}} 
                                              {:id   :straightening
                                               :name "Straightening"
                                               :type :digit_input
                                               :min  {:type  :num 
                                                      :value 0}
                                               :max  {:type  :subscribe 
                                                      :value [:formdata :quantity]}}]}
                                    {:id   "diamond_cut"
                                     :name "Diamond cut"
                                     :type :radio_button}
                                    {:id        "tyre_services"
                                     :name      "Tyre services"
                                     :condition #(= % 1)
                                     :type      :checkbox_group
                                     :options   ["Tyre removal & refitting" "Tyre fitting"]}]}})                         

# COLOR PROTOTYPE
[{:id       "color-id-1" 
  :gallery  [{:src "/img/color_gallery/clear-vision-and-misty-blue-nismo-wheels-thumbnail.png"}
                {:src "/img/color_gallery/creation-heavy-silver-with-lollypop-blue-top-coat-5-thumbnail.png"}
                {:src "/img/color_gallery/creation-powder-coated-ford-f250-pmf-truck-thumbnail.png"}
                {:src "/img/color_gallery/intense-blue-over-super-chrome-1-thumbnail.png"}
                {:src "/img/color_gallery/powder-coated-blue-and-black-two-toned-american-force-wheels-thumbnail.png"}]
  :src       "/img/color_samples/aqaparlat.png"
  :basecolor "green"
  :surface   "F"
  :price     (:basic :basic-plus :pro :pro-plus)

  :config   {:texture     {"map"       "media/textures/hammer/map.png"
                           "normalMap" "media/textures/hammer/normalMap.png}
             :displacement 0.12}

  :bio       "A párlat széria minden tagja különösen selymes tapintással rendelkezik, kinézetre leginkább hajnali párával borított metál-fényezésre hasonlít."}

# STATUS KEYWORDS

 [:pending
  :new
  :active
  :in-progress
  :complete
  ]

# TAG PROTOTYPE

 {:id    (string || ObjectID)
  :name  (string)    
  :color (string)}

# MACHINE PROTOTYPE

 {:id           (string || ObjectID)
  :name         (string)
  :power {:source     [:electricity :gas :other]
          :concumtion (int)
  
  :type         (:oven :sandblaster)
  :formdata     {:max-temp (int)
                 :dimension {:height (int)
                             :width  (int)
                             :length (int)}}}


# MACHINE EXAMPLE

[
 {:id           "oven-1"
  :name         "Oven 1"
  :type         :oven
  :power-source "áram"

  :setting {}}
]

{
 :type         [:oven :snadblaster :coating-booth :asd-1]
 :power-source [:electricity :gas :other]
 :power        (int) ;;100
}

# WORKSPACE PROTOTYPE

{:id          (string || ObjectID)
 :owner       (user-id)
 :name        (string)
 :shared-with (hashmap of user-roles)
 :qids        {QID (owner-user-id)   alapból tartlamaz egy QID-ET ami a az owner user-id-ra mutat
               QID (user-id)}
 :bussiness-info    {}
 :subscription-type (subs-id)

 }

:qids szerepe hogy gyors bejelentkezésnél/átjelentkezésnél a megadott QID rámutat egy user-id-ra
és ez alapján tudja a workspace eldönteni hogy az adott user bejelentkezhet e 

Ha egy usernek nincsen jogosultág adva az adott modulhoz akkor alapértelmezetten nincs jogosultásga.

account update 
account/workspaces {WID    workspace-id  
                    QID    qid}

# WORKSPACE EXAMPLE

 {:id           (random-uuid)
  :name         "My little workspace"
  :owner        "User-acount-id-1"
  :qids         {"qids12" "User-acount-id"}
  :shared-with  {user-id-1 'role-id'
                 user-id-2 {:clients.editor "rw"}}
 }

{user-id-1 {:clients.editor "r"
            :clients.lister "rw"}
 user-id-2 {:clients.editor "rw"}}

 # Compact pattern for roles
  Egy kompakt adat szerkezet role kezelésekhez
  Az adatszerkezet egy JWT-ben lenne tárolva.
  A szerveren lenne egy parser/checker/validator/permission-gate ami felelne azért hogy 
  a be érkezet pattern alapján eldöntse hogy a user jogosult az adott module adataihoz.
  Array
  ["c.l-rw" "c.e-r"] => {:clients.lister "rw" :clients.editor "r"}
  String
  "c.l-rw|c.e-rw|o.l-r" => {:clients.lister "rw" :clients.editor "rw" :orders.lister "r"}

# EMPLOYEE ID (QID)
  Minden account kap egy QID-t (6 jegyű kód) workspace behívásakor, ami lehetővé teszi a user switchet az workspace.
  A QID csak az adott workspacehez ad bejelentkezést.
  Ha bármilyen másik module-ra (route-ra), akar a user menni ahoz teljes belépés szükséges.
  Rossz QID esetén lehetőség email password belépésre 2fa nélkül.

# LOCATION PROTOTYPE

 {:id          (string || ObjectID)
  :name        (string)
  :description (string)
  :external?   (boolean)
  :place       (string)
  :coordinates (geojson object)
  :tags        (tags)  
  :link        (id)
  :machines    (vector of machines)
  :roles       (vector of role-id)
 }

# Station PROTOTYPE

 {:id          (string || ObjectID)
  :name        (string)
  :description (string)
  :external?   (boolean)
  :place       (string)
  :tags        (tags)  
  :location    (location-id)
  :machines    (vector of machines)
  :roles       (vector of role-id)
 }

Authorize user by role
Station megnyitásakor ellenőrzi a rendszer, hogy rendelkezik a stationhöz szükséges role-al.

place: External, Internal location-nek is lehet helyszíne egyformán.
       Példa: "B épület", "Városneve", "Kuka mögöttt a sikátorban", "1234 Kecskemét, Hegesztő utca 420." "akármi"
      
tags:  
       A tagek alapján vannak csoportosítva a location-nek, azokban a select-ekben,
       ahol location-ket lehet kiválasztani, mert egy cég akár több 100-200 location-t is felvihet, ha úgy dönt.
       Például: Az Inventory-ban a polcokat külön location-ként viszi fel, és tag-ként megadhatja,
                hogy az adott polc például melyik raktárban van. 
                így a selection-ben raktáronként group-olva jelennek meg a polcok.

# LOCATION(Station) EXAMPLE

 {:id          :location-id-1
  :name        "Welding Place 1"
  :description "Place for welding."
  :external?   false
  :place       "Main building"
  :tags        ["Tag1" "welding-loc"]
  :machines    []
  }

 {:id          :location-id-2
  :name        "Welding Place 2"
  :description "Place for welding."}

# WORK PROCESS PROTOTYPE

 {:label     (string)
  :locations (loaction-id in vector)
  :qty       (int)
 }

# BATCH PROTOTYPE

 {:id               (string || ObjectID)
  :quantity         100
  :workflow         [{:label "welding"  :location :loaction-id-1 :qty 50}
                     {:label "welding"  :location :loaction-id-2 :qty 50}
                     {:label "grinding" :location :loaction-id-3 :qty 0}
                     {:label "painting  :location :loaction-id-4 :qty 0}]
  :items-done-count (int)
  :status           ()
 }

# ITEM PROTOTYPE

{:id       (string || ObjectID)
 :status   (status-keyword)
 :location (loaction)
 :name     (string)
 :formdata (hashmap)
 :metadata (hashmap)
}

# JOB PROTOTYPE

{:id        (server-random-uuid)
 :name      "part-name"
 :precursor ""
 :status   (status-keyword)
 :location (location)
 :form      {:id   "form-id
             :data (formdata)}
 :total     (total-price)}

# GROUP/NESTED JOB
{:id       (server-random-uuid)
 :name     "part-name"
 :status   (status-keyword)
 :location (location)
 :parts    ()}

# ORDER PROTOTYPE

{:id       (string || ObjectID)
 :status   (status-keyword)
 :location (loaction)
 :jobs     (vector of jobs)
 :metadata (hashmap)
 }



Add price quote calculator to fromdata

- select calc-type"function" [*,-,+,/]
- add params to calc
- calc can be params too

Inputs: 
 - digit          {:val (num)}
 - select         {:val (num)}
 - radio button   {:val (num)}
 - checkbox       {:val (num)}

2 + 2 + 2 * 2 - 2 = 6
(+ 2 2 (* 2 2))

(- value-path (+ value-path 1))

{:quantity  4
 :condition {:label "Coated" :value 50}
 :material  {:label "Steel"  :value 40}
 :repair    {:welding {:qty 3 :value 90}}
 :size      {:label "18"     :value 1.8}}

":quantity * ((:material + :condition + :repair.welding) + (:diamond_cut * :size))"

"4 * ((90 + 50) * 1.8)"

Example
(* [:formdata :quantity] 
   (* [:formdata :size] 
      (+ [:formdata :condition] [:formdata :coating])))