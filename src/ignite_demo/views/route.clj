(ns ignite-demo.views.route
  "Generates HTML for a route page."
  (:use [hiccup.page :only (html5 include-css)]
        korma.core
        korma.db)
  (:require [ignite-demo.models.route :as r]
            [ignite-demo.models.direction :as d]
            [ignite-demo.views.layout :as layout]))

(declare display-direction
         path-params-for-stops
         markers-params-for-stops)

(defn display
  "Creates the HTML for a given route-tag."
  [route-tag]
  (let [route (r/route-for-tag route-tag)
        {:keys [title latmax latmin lonmax lonmin]} route
        directions (r/directions-for-route route-tag)]
    (layout/page title
     [:h3 title]
     [:div
      [:table {:border 1 :cellpadding 5}
       [:tr [:th] [:th "max"] [:th "min"]]
       [:tr [:th "lat"] [:td latmax] [:td latmin]]
       [:tr [:th "lon"] [:td lonmax] [:td lonmin]]]]
     [:div
      (map display-direction directions)]
     [:a {:href "/"} "home"])))

(defn display-direction
  "Helper method that displays a list of stops for a given direction."
  [direction]
  (let [{:keys [name title id]} direction
        stops (d/stops-for-direction id)]
    [:div {:style "padding: 10px;"}
     [:h3 title]
     [:img {:height 400
            :width 400
            :src
            (str "http://maps.googleapis.com/maps/api/staticmap"
                 "?size=400x400"
                 "&maptype=roadmap"
                 (path-params-for-stops stops)
;                 (markers-params-for-stops stops)
                 "&sensor=false")}]
     [:ul
      [:ul
       (map #(vector :li [:a {:href (str "/stop/" (:id %))}
                          (:title %)])
            stops)]]]))

(defn path-params-for-stops
  "Takes a seq of stops and returns the &path=... param string to be used with the Google Maps Static Map API."
  [stops]
  (apply str "&path=color:0x0000ff|weight:5"
         (map (fn [stop]
                (str "|" (:lat stop) "," (:lon stop)))
              stops)))

(defn markers-params-for-stops
  "Takes a seq if stops and returns the &markers... param string to be used with the Google Maps Static Map API"
  [stops]
  (apply str
         (map (fn [stop]
                (str "&markers=color:blue|label:|" (:lat stop) "," (:lon stop)))
              stops)))
