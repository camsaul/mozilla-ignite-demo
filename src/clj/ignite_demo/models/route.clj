(ns ignite-demo.models.route
  "Helper methods for getting and dealing with routes."
  (:use korma.db
        korma.core)
  (:require [ignite-demo.models.db :as db]
            [ignite-demo.models.utility :as util]
            [ignite-demo.models.direction :as dir]))

(defn all
  "Returns a seq of all routes."
  []
  (select db/route))

(defn route-for-tag
  "Returns the route for the route-tag"
  [route-tag]
  (first (select db/route (where {:id route-tag}))))

(defn directions-for-route
  "Returns a seq of directions for a given route-tag"
  [route-tag]
  (select db/direction (where {:route_id route-tag})))

(defn all-stops-for-route
  "Returns a seq of all stops that are served by a route"
  [route-tag]
     (let [directions (map :id (select db/direction (where {:route_id route-tag})))]
       (first (map dir/stops-for-direction directions))))

(defn closest-stop-along-route
  "Returns the closest stop along a certain route to a location specified by lat/lon"
  [route-tag lat lon]
  (let [all-stops (all-stops-for-route route-tag)]
    (if-not (empty? all-stops)
      (let [stops (map (fn [stop]
                         (vector (util/distance-between-coordinates lat lon (:lat stop) (:lon stop)) stop))
                       (all-stops-for-route route-tag))]
        (second (reduce (fn [s1 s2] (if (< (first s1) (first s2)) s1 s2)) stops))))))

;;; 5xx = xx limited (L) route
;;; 6xx = xx owl route
;;; 7xx = xx express route
;;; 8xx = xx bx express route
;;; 9xx = xx ax express route
(defn regular-route-tag-for-passenger-count-route-tag
  "The passenger count CSV data provided by the SFMATA uses different route numbers than what the routes are actually called. This function translates the names used in passenger-count to what we use everywhere else."
  [route-tag]
  (let [route-tag-as-int (try (Integer/parseInt route-tag) (catch NumberFormatException e))]
    (if (or (nil? route-tag-as-int) (< route-tag-as-int 500))
      route-tag
      (let [suffixes {5 "L", 6 " OWL", 7 "X", 8 "BX", 9 "AX" }
            hundreds (quot route-tag-as-int 100)
            prefix (rem route-tag-as-int 100)]
        (str prefix (suffixes hundreds))))))
