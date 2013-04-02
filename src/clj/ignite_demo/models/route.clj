(ns ignite-demo.models.route
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
