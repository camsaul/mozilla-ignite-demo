(ns ignite-demo.views.index
  "View for the index page."
  (:use [hiccup.page :only (html5 include-css)]
        [hiccup.core :only (html)]
        korma.db
        korma.core
        ignite-demo.views.map-images)
  (:require [ignite-demo.models.route :as r]
            [ignite-demo.models.direction :as d]
            [ignite-demo.views.layout :as layout]))

(defn display
  "Generates the HTML for the index page."
  []
  (layout/page "Routes List"
   [:h1 "All Routes"]
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
              (r/all))]))
