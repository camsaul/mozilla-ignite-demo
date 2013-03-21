(ns ignite-demo.views.route
  "Generates HTML for a route page."
  (:use [hiccup.page :only (html5 include-css)]
        [hiccup.core :only (html)]
        korma.core
        korma.db
        ignite-demo.views.map-images)
  (:require [ignite-demo.models.route :as r]
            [ignite-demo.models.direction :as d]
            [ignite-demo.views.layout :as layout]))

(declare display-direction)

(defn display
  "Creates the HTML for a given route-tag."
  [route-tag]
  (let [route (r/route-for-tag route-tag)
        {:keys [title latmax latmin lonmax lonmin]} route
        directions (r/directions-for-route route-tag)]
    (layout/page title
     [:h1 title]
     [:div
      (map display-direction directions)])))

(defn display-direction
  "Helper method that displays a list of stops for a given direction."
  [direction]
  (let [{:keys [name title id]} direction
        stops (d/stops-for-direction id)
        map-size 460]
    [:div {:style "padding: 10px;"}
     [:h3 title]
     [:div {:class "row"}
      [:div {:class "span4"}
       [:ul
        [:ul
         (map (fn [stop]
                (vector :li [:a {:href (str "/stop/" (:id stop)) :data-toggle "tooltip"
                                 :data-placement "right"
                                 :data-html "true"
                                 :data-original-title (html (map-image-for-stop
                                                             (:lat stop) (:lon stop) 175 15))}
                             (:title stop)]))
              stops)]]]
      [:div {:class "span6"}
       (map-image-for-stops map-size stops)]]]))
