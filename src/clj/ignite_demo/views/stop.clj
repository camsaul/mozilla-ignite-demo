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
            [ignite-demo.views.layout :as layout]
            [ignite-demo.views.graph :as graph]
            [ignite-demo.models.utility :as util]
            [clojure.math.numeric-tower :as math]))

(declare display-arrival-charts display-passenger-counts display-passenger-counts-graph)

(defn display
  "Generates the HTML for a given stop-tag."
  [stop-tag]
  (let [the-stop (s/stop-for-tag stop-tag)
        routes (s/routes-that-serve-stop stop-tag)
        {:keys [title lat lon]} the-stop
        map-size 380]
    (layout/page title
     [:h1 title]
     [:div.row
      [:div.span5
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
      [:div.span5
       (map-image-for-stop lat lon map-size)]]
     [:div.row
      (display-passenger-counts stop-tag)]
     [:div.row
      [:div.span10
       (display-arrival-charts stop-tag)]])))

(defn display-arrivals-chart-for-route
  "Displays the arrival time table for a route."
  [[route-id times]]
  (let [pairs (map (fn [[_ hour diff]]
                     (let [late-str (if (< diff 0) "late" "early")
                           [mins-late secs-late] (map #(biginteger (math/abs(% diff 60))) [quot rem])]
                       [(format "%s: %d:%02d %s" (util/format-time hour) mins-late secs-late late-str)
                        (math/abs diff)]))
                   times)]
    [:div.arrivals-chart {:route route-id}
     (graph/display-vertical-bar-graph (str "Route " route-id " Average Arrival Times") pairs 780 20)]))

(defn display-arrival-charts
  "Generates the HTML for the scheduled/actual arrivals table for a stop."
  [stop-tag]
  `[:div ~@(mapv display-arrivals-chart-for-route
                 (group-by first (s/arrival-times-for-stop stop-tag)))])

(defn process-passenger-counts
  "Helper method to do preprocessing of passenger counts for display-passenger-counts. Returns a vector of
   [route [hour avg-est-load]+] vectors sorted by hour."
  [stop-tag]
  (let [all-maps (group-by :route_id
                           (map #(update-in % [:route_id] r/route-tag-for-pcount-route-tag)
                                (s/passenger-counts-for-stop stop-tag)))]
    (mapv (fn [[route maps]]
            (let [all-hour-maps (group-by :hour (sort-by :hour maps))]
              `[~route ~@(mapv (fn [[hour hour-maps]]
                                 (let [avg-pcount (float (/ (reduce + (map :est_load hour-maps))
                                                            (count hour-maps)))]
                                   [(util/format-time hour) avg-pcount]))
                               all-hour-maps)]))
          all-maps)))

(defn display-passenger-counts
  "Generates the HTML for show the passenger counts table for a stop."
  [stop-tag]
  (let [pcounts (process-passenger-counts stop-tag)]
    `[:div#passenger-count.span10
      [:div.btn-group ~@(mapv (fn [[route-tag _]] [:button.btn {:route route-tag} route-tag])
                              pcounts)]
      ~@(mapv (fn [[route-tag & counts]]
                [:div.route-table {:route route-tag}
                 (graph/display-vertical-bar-graph
                  (str "Route " route-tag " Average Passenger Counts") counts 780 20)])
              pcounts)]))
