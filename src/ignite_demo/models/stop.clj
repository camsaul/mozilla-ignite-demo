(ns ignite-demo.models.stop
  (:use korma.db
        korma.core)
  (:require [ignite-demo.models.utility :as util]
            [ignite-demo.models.db :as db]))

(def all-stops (select db/stop))

(defn stop-for-tag
  "Returns the stop with the given stop-tag"
  [stop-tag]
  (first (select db/stop
                 (where {:id stop-tag}))))

(defn stops-within-range
  "Returns a seq of all stops within threshold-meters from the location specified by lat/lon."
  [lat lon threshold-meters]
  (filter #(< (util/distance-between-coordinates lat lon (:lat %) (:lon %))
              threshold-meters)
          all-stops))

(defn directions-that-serve-stop
  "Returns a seq of directions that serve a given stop-tag."
  [stop-tag]
  (select db/direction_stop
          (where {:stop_id stop-tag})
          (fields) ; don't return any of the direction_stop fields
          (with db/direction)))

(defn routes-that-serve-stop
  "Returns a seq of routes that serve a given stop-tag"
  [stop-tag]
  (let [directions (directions-that-serve-stop stop-tag)
        route-tags (map :route_id directions)]
    (select db/route
            (where (apply or (map #(assoc {} :id %) route-tags))))))
