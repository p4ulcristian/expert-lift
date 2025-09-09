(ns app.backend.favicons)

(defn favicons []
  (list
   [:link {:rel "icon"
           :type "image/png"
           :href "/favicon/16x16.png"
           :sizes "16x16"}]
   [:link {:rel "icon"
           :type "image/png"
           :href "/favicon/32x32.png"
           :sizes "32x32"}]
   [:link {:rel "icon"
           :type "image/png"
           :href "/favicon/64x64.png"
           :sizes "64x64"}]
   [:link {:rel "icon"
           :type "image/png"
           :href "/favicon/128x128.png"
           :sizes "128x128"}]
   [:link {:rel "icon"
           :type "image/png"
           :href "/favicon/192x192.png"
           :sizes "192x192"}]
   [:link {:rel "icon"
           :type "image/png"
           :href "/favicon/512x512.png"
           :sizes "512x512"}]
   [:link {:rel "apple-touch-icon"
           :type "image/png"
           :href "/favicon/180x180.png"
           :sizes "180x180"}]))