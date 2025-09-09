(ns features.customizer.panel.backend.data)

(def static-tree
  {;; Root categories
   #uuid "ecf9ae00-7c0c-4a2c-8579-8eceb18fa180"
   {:id #uuid "ecf9ae00-7c0c-4a2c-8579-8eceb18fa180"
    :name "City"
    :description ""
    :picture_url nil
    :type "category"
    :children {}}

   #uuid "dde0539c-2763-4a8b-98a7-9be11334333f"
   {:id #uuid "dde0539c-2763-4a8b-98a7-9be11334333f"
    :name "Garden"
    :description "Garden Garden anyamot"
    :picture_url nil
    :type "category"
    :children {
               #uuid "23dc95dd-ab3d-4c87-ac4e-aecacdca12d2"
               {:id #uuid "23dc95dd-ab3d-4c87-ac4e-aecacdca12d2"
                :name "Kertiszerszamok"
                :description ""
                :picture_url nil
                :type "category"
                :children {
                           #uuid "10711f0b-3b53-462c-a291-f1e08b8a8ab1"
                           {:id #uuid "10711f0b-3b53-462c-a291-f1e08b8a8ab1"
                            :name "vasvilla"
                            :description ""
                            :picture_url nil
                            :type "part"
                            :prefix nil
                            :model_url nil
                            :mesh_id nil
                            :popular nil
                            :package_id nil
                            :category_id #uuid "23dc95dd-ab3d-4c87-ac4e-aecacdca12d2"}}}}}
       
    

   #uuid "f60cc6e7-4119-427a-adde-07579f875cad"
   {:id #uuid "f60cc6e7-4119-427a-adde-07579f875cad"
    :name "Home"
    :description ""
    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/140b6193-d3b1-4839-b05f-0a2a14465d39.svg"
    :type "category"
    :children {}}

   #uuid "0faf309c-2724-4c14-89ff-ae5c826e9457"
   {:id #uuid "0faf309c-2724-4c14-89ff-ae5c826e9457"
    :name "Industry"
    :description ""
    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/9d9650b9-15e0-4530-b37c-c50eb785e0ce.svg"
    :type "category"
    :children {}}

   #uuid "945ccb28-28b7-4606-976f-15a090ef64e3"
   {:id #uuid "945ccb28-28b7-4606-976f-15a090ef64e3"
    :name "Music"
    :description ""
    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/b0a16561-b5db-4b39-9204-77fe6de4a731.svg"
    :type "category"
    :children {}}

   #uuid "afbb0490-c979-46d2-8c3a-0b7f368fd57e"
   {:id #uuid "afbb0490-c979-46d2-8c3a-0b7f368fd57e"
    :name "Pets"
    :description ""
    :picture_url nil
    :type "category"
    :children {
               #uuid "7f2b8b6e-e059-4b30-b240-5ee3ec528112"
               {:id #uuid "7f2b8b6e-e059-4b30-b240-5ee3ec528112"
                :name "subcateg"
                :description ""
                :picture_url nil
                :type "category"
                :children {}}}}
    

   #uuid "d7145cc4-a0df-4a0a-ab3f-771bf5ecf287"
   {:id #uuid "d7145cc4-a0df-4a0a-ab3f-771bf5ecf287"
    :name "Sports"
    :description ""
    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/53662423-f5d8-437d-b751-847efa16567f.svg"
    :type "category"
    :children {}}

   #uuid "e9d7464e-fd5d-4449-ba56-fd657834084b"
   {:id #uuid "e9d7464e-fd5d-4449-ba56-fd657834084b"
    :name "Vehicles"
    :description ""
    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/02ce7709-ce66-4fde-a7d9-bad0a04d71dd.svg"
    :type "category"
    :children {
               #uuid "bfb23f44-7bfe-443d-a96f-9ebd7500f6fa"
               {:id #uuid "bfb23f44-7bfe-443d-a96f-9ebd7500f6fa"
                :name "Bicycle"
                :description ""
                :picture_url nil
                :type "category"
                :children {
                           #uuid "ca5c5d14-8790-4ec2-b9d2-fde17b840e5f"
                           {:id #uuid "ca5c5d14-8790-4ec2-b9d2-fde17b840e5f"
                            :name "Bike"
                            :description ""
                            :picture_url nil
                            :type "category"
                            :children {
                                       #uuid "84b2bf28-e6f0-4910-b356-470b25c573a4"
                                       {:id #uuid "84b2bf28-e6f0-4910-b356-470b25c573a4"
                                        :name "Downhill"
                                        :description "Downhill description"
                                        :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/50936e2a-0bf0-445f-9f5a-84608ae18c15.svg"
                                        :type "package"
                                        :prefix nil
                                        :model_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/e48c38c2-a5c9-4ad3-8dda-09c4f3890b22.glb"
                                        :mesh_id nil
                                        :popular nil
                                        :package_id nil
                                        :category_id #uuid "ca5c5d14-8790-4ec2-b9d2-fde17b840e5f"
                                        :children {
                                                   #uuid "f3e86214-facd-41ae-85cd-e76aace97b4b"
                                                   {:id #uuid "f3e86214-facd-41ae-85cd-e76aace97b4b"
                                                    :name "Frame"
                                                    :description "downhill frame"
                                                    :picture_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/88d6f44c-0012-4772-88d6-29a01bab7276.svg"
                                                    :type "part"
                                                    :prefix "Downhill"
                                                    :model_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/e48c38c2-a5c9-4ad3-8dda-09c4f3890b22.glb"
                                                    :mesh_id "frame"
                                                    :popular nil
                                                    :package_id #uuid "84b2bf28-e6f0-4910-b356-470b25c573a4"
                                                    :category_id nil}}}}}}}
             
          
       
               #uuid "d50ee0e1-b2ec-48b6-a6cc-c1f925e8f575"
               {:id #uuid "d50ee0e1-b2ec-48b6-a6cc-c1f925e8f575"
                :name "Car"
                :description ""
                :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/02ce7709-ce66-4fde-a7d9-bad0a04d71dd.svg"
                :type "category"
                :children {
                           #uuid "cbd74c42-cfa7-43f7-a3da-e412edcdbb9e"
                           {:id #uuid "cbd74c42-cfa7-43f7-a3da-e412edcdbb9e"
                            :name "Wheel"
                            :description ""
                            :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/a6df589c-2b76-4687-a58c-de109ede44a2.svg"
                            :type "category"
                            ;; :prefix nil
                            ;; :model_url "https://bucket-production-f0da.up.railway.app:443/demo/606dccc2-b31e-4b43-b765-1af9ebd39fba.glb"
                            ;; :mesh_id nil
                            ;; :popular nil
                            ;; :form_id #uuid "26dbab8d-f56e-4b9e-990b-750b2ea5470d"
                            ;; :package_id nil
                            :category_id #uuid "d50ee0e1-b2ec-48b6-a6cc-c1f925e8f575"
                            :children {
                                       #uuid "ffffffff-ffff-ffff-ffff-ffffffffffff"
                                       {:id #uuid "ffffffff-ffff-ffff-ffff-ffffffffffff"
                                        :name        "One piece rim"
                                        :description "one piece rim package"
                                        :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/a6df589c-2b76-4687-a58c-de109ede44a2.svg"
                                        :type        "package"
                                        :prefix      nil
                                        :model_url   nil
                                        :mesh_id     nil
                                        :popular     nil
                                        :package_id  nil
                                        :category_id #uuid "cbd74c42-cfa7-43f7-a3da-e412edcdbb9e"
                                        :children    {#uuid "3b53ddab-08ed-4bf1-bde2-578ed806b5f1"
                                                       {:id #uuid "3b53ddab-08ed-4bf1-bde2-578ed806b5f1"
                                                        :name "One piece rim"
                                                        :description "This is a rim"
                                                        :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/a6df589c-2b76-4687-a58c-de109ede44a2.svg"
                                                        :type "part"
                                                        :prefix nil
                                                        :form_id #uuid"75f20f1d-60ec-4eaa-8aeb-c0d5a10bcebd"
                                                        :model_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/a74c4971-e2ba-4d16-bce9-317643118077.glb"
                                                        :mesh_id #uuid "3b53ddab-08ed-4bf1-bde2-578ed806b5f1"
                                                        :popular nil
                                                        :package_id #uuid "ffffffff-ffff-ffff-ffff-ffffffffffff"
                                                        :category_id #uuid "d50ee0e1-b2ec-48b6-a6cc-c1f925e8f575"}}}
          
       
    

   ;; Standalone packages and parts not attached to a category
                                       #uuid "d250385a-a426-4631-8871-74833cc5df36"
                                       {:id #uuid "d250385a-a426-4631-8871-74833cc5df36"
                                        :name "Three piece rim"
                                        :description "This is a 3 piece rim"
                                        :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/45924280-9dac-4614-8012-a25e866beb96.svg"
                                        :type "package"
                                        :prefix nil
                                        :model_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/22c9855c-85ba-42a8-b629-99c0e2209d95.glb"
                                        :mesh_id nil
                                        :form_id #uuid "3ae6f595-29c4-4d80-9aa7-4a972f35384e"
                                        :popular nil
                                        :package_id #uuid "cbd74c42-cfa7-43f7-a3da-e412edcdbb9e"
                                        :category_id nil
                                        :children {
                                                   #uuid "c6092ebd-4daf-4f8e-b10b-7bc74a87db75"
                                                   {:id #uuid "c6092ebd-4daf-4f8e-b10b-7bc74a87db75"
                                                    :name "Barrel"
                                                    :description ""
                                                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/a1b93b27-1158-4e18-af37-a4e097bf299a.svg"
                                                    :type "part"
                                                    :prefix nil
                                                    :form_id #uuid "dc0aacf1-e75d-4fdc-9b1c-ccad0e54725c"
                                                    :model_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/22c9855c-85ba-42a8-b629-99c0e2209d95.glb"
                                                    :mesh_id "barrel"
                                                    :popular nil
                                                    :package_id #uuid "d250385a-a426-4631-8871-74833cc5df36"
                                                    :category_id #uuid "d50ee0e1-b2ec-48b6-a6cc-c1f925e8f575"}
                                                   #uuid "b18f2ccb-2e71-462d-acc3-2b5c27251bd1"
                                                   {:id #uuid "b18f2ccb-2e71-462d-acc3-2b5c27251bd1"
                                                    :name "Face"
                                                    :description ""
                                                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/74df8760-dde7-4957-b65d-3d8ffc194eb0.svg"
                                                    :type "part"
                                                    :prefix nil
                                                    :model_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/22c9855c-85ba-42a8-b629-99c0e2209d95.glb"
                                                    :mesh_id "face"
                                                    :form_id #uuid "dc0aacf1-e75d-4fdc-9b1c-ccad0e54725c"
                                                    :popular nil
                                                    :package_id #uuid "d250385a-a426-4631-8871-74833cc5df36"
                                                    :category_id nil}
                                                   #uuid "d22b4515-f744-4349-b85f-265bf6f34bad"
                                                   {:id #uuid "d22b4515-f744-4349-b85f-265bf6f34bad"
                                                    :name "Lip"
                                                    :description ""
                                                    :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/0a6f84d6-4801-4589-ae61-f9549d5512e5.svg"
                                                    :type "part"
                                                    :prefix nil
                                                    :model_url "https://bucket-production-f0da.up.railway.app:443/ironrainbow/22c9855c-85ba-42a8-b629-99c0e2209d95.glb"
                                                    :mesh_id "lip"
                                                    :form_id #uuid "dc0aacf1-e75d-4fdc-9b1c-ccad0e54725c"
                                                    :popular nil
                                                    :package_id #uuid "d250385a-a426-4631-8871-74833cc5df36"
                                                    :category_id nil}}}
    

                                       #uuid "5d682029-539a-4456-ba2b-70a335dc157c"
                                       { :id #uuid "5d682029-539a-4456-ba2b-70a335dc157c"
                                        :name "Two piece rim"
                                        :description ""
                                        :picture_url "https://bucket-production-f0da.up.railway.app:443/demo/0d4b974d-6c85-4446-be97-40f01ff70a7c.svg"
                                        :type "package"
                                        :prefix nil
                                        :model_url nil
                                        :mesh_id nil
                                        :popular nil
                                        :package_id #uuid "cbd74c42-cfa7-43f7-a3da-e412edcdbb9e"
                                        :category_id nil
                                        :children {}}}}}}}}})
  
  