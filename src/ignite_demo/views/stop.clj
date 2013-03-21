(ns ignite-demo.views.stop
  "Generates HTML for a stop page."
  (:use [hiccup.page :only (html5 include-css)]
        korma.core
        korma.db)
  (:require [ignite-demo.models.stop :as s]))


(defn display
  "Generates the HTML for a given stop-tag."
  [stop-tag]
  (let [the-stop (s/stop-for-tag stop-tag)
        routes (s/routes-that-serve-stop stop-tag)
        {:keys [title lat lon]} the-stop]
    (html5
     [:head
      (include-css "/stylesheets/base.css")]
     [:body
      [:div {:id "wrapper"}
       [:h3 title]
       [:p (str "Coordinates: " lat ", " lon)]
       [:p (str "Stop ID: " stop-tag)]
       [:img {:height 400
              :width 400
              :src
              (str "http://maps.googleapis.com/maps/api/staticmap"
                   "?center=" lat "," lon
                   "&zoom=18"
                   "&size=400x400"
                   "&maptype=roadmap"
                   "&markers=color:blue%7Clabel:%7C" lat "," lon
                   "&sensor=false")}]
       [:div
        [:h3 "Routes that Serve this stop:"]
        [:ul 
         (map (fn [route]
                (vector :li [:a
                             {:href (str "/route/" (:id route))}
                             (:title route)]))
              routes)]]
       [:a {:href "/"} "home"]]])))
