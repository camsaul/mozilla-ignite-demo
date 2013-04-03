(ns ignite-demo.views.route
  "Generates HTML for a route page."
  (:use [hiccup.page :only (html5 include-css)]
        [hiccup.core :only (html)]
        korma.core
        korma.db
        ignite-demo.views.map-images)
  (:require [ignite-demo.models.route :as r]
            [ignite-demo.models.utility :as util]
            [ignite-demo.models.direction :as d]
            [ignite-demo.views.layout :as layout]
            [ignite-demo.views.graph :as graph]
            [clojure.math.numeric-tower :as math]))

(declare display-direction display-pcount-graph display-arrivals-graph)

(defn display
  "Creates the HTML for a given route-tag."
  [route-tag]
  (let [route (r/route-for-tag route-tag)
        {:keys [title latmax latmin lonmax lonmin]} route
        directions (r/directions-for-route route-tag)]
    (layout/page title
     [:h1 title]
     [:div
      (map (partial display-direction route-tag) directions)])))

(defn display-direction
  "Helper method that displays a list of stops for a given direction."
  [route-tag {:keys [name title id]}]
  (let [stops (d/stops-for-direction id)
        pcount-graph (display-pcount-graph route-tag stops)
        stops-list [:ul {:style "margin: 10px;"}
                    (map (fn [{:keys [id lat lon title]}]
                           (vector :li [:a {:href (str "/stop/" id) :data-toggle "tooltip"
                                            :data-placement "right"
                                            :data-html "true"
                                            :data-original-title (html (map-image-for-stop lat lon 175 15))}
                                        title]))
                         stops)]]
    [:div.row {:style "padding: 10px;"}
     [:h3 title]
     [:div.span4
      (map-image-for-stops 300 stops)
      (if pcount-graph stops-list)]
     pcount-graph
     (display-arrivals-graph route-tag stops)
     (if (not pcount-graph) [:div.span4 stops-list])]))

(defn display-pcount-graph
  "Helper method to display a graph of passenger counts for a direction."
  [route-tag stops]
  (let [pcounts (flatten (r/passenger-counts-for-route route-tag))]
    (if (empty? pcounts)
      nil
      (let [pcounts (apply assoc {} pcounts)
            pairs (filterv #(not (nil? (last %)))
                           (map (fn [{:keys [id title]}]
                                  (let [pcount (pcounts id)]
                                    [(format "%s: %.1f" title pcount) pcount]))
                                stops))]
        [:div.span6 (graph/display-vertical-bar-graph "Average Passenger Counts Per Stop" pairs  460 20)]))))

(defn display-arrivals-graph
  "Helper method to display a graph of average late/early times for a direction"
  [route-tag stops]
  (let [arrival-times (r/arrival-times-for-route route-tag)
        pairs (->> (mapv (fn [{:keys [id title]}]
                           (if-let [arrival (arrival-times id)]
                             [(format "%s: %s" title (util/format-late-early-string arrival))
                              (math/abs arrival)]))
                         stops)
                   (filter #(not (nil? %))))]
    (if (not (empty? pairs))
      [:div.span6 (graph/display-vertical-bar-graph "Average Arrival Times Per Stop" pairs 500 20)])))
