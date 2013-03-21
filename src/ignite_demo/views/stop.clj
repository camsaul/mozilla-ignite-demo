(ns ignite-demo.views.stop
  "Generates HTML for a stop page."
  (:use [hiccup.page :only (html5 include-css)]
        [hiccup.core :only (html)]
        korma.core
        korma.db
        ignite-demo.views.map-images)
  (:require [ignite-demo.models.stop :as s]
            [ignite-demo.models.route :as r]
            [ignite-demo.models.direction :as d]
            [ignite-demo.views.layout :as layout]))

(defn display
  "Generates the HTML for a given stop-tag."
  [stop-tag]
  (let [the-stop (s/stop-for-tag stop-tag)
        routes (s/routes-that-serve-stop stop-tag)
        {:keys [title lat lon]} the-stop
        map-size 460]
    (layout/page title
     [:h1 title]
     [:div {:class "row"}
      [:div {:class "span4"}
       [:p (str "Coordinates: " lat ", " lon)]
       [:p (str "Stop ID: " stop-tag)]
       [:div
        [:h3 "Routes that Serve this stop:"]
        [:ul 
         (map (fn [route]
                (let [direction (first (r/directions-for-route (:id route)))
                      stops (d/stops-for-direction (:id direction))]
                  (vector :li [:a {:href (str "/route/" (:id route))
                                   :data-toggle "tooltip"
                                   :data-placement "right"
                                   :data-html "true"
                                   :data-original-title (html (map-image-for-stops 175 stops))}
                               (:title route)])))
              routes)]]]
      [:div {:class "span6"}
       (map-image-for-stop lat lon map-size)]])))
