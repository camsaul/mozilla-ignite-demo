(ns ignite-demo.models.stop
  (:use korma.db
        korma.core)
  (:require [ignite-demo.models.utility :as util]
            [clojure.java.jdbc :as sql]
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

(defn arrival-times-for-stop
  "Returns a seq of maps keyed by :route_id, :scheduled_arrival and :real_arrival for a given stop"
  [stop-tag]
  (db/with-conn
    (sql/with-query-results results
      [(str "select sa.route_id, "
            "cast (extract(hour from sa.arrival_time) as integer) as hour, "
            "cast (extract(seconds from (sa.arrival_time - ra.arrival_time)) as integer) as diff  "
            "from scheduled_arrival as sa  "
            "left join realtime_arrival as ra  "
            "on sa.block_id = ra.block_id  "
            "and sa.route_id = ra.route_id  "
            "and sa.trip_id = ra.trip_id  "
            "and sa.stop_id = ra.stop_id  "
            "where sa.stop_id = '" stop-tag "' "
            "and sa.arrival_time is not null  "
            "and ra.arrival_time is not null  "
            "and extract(day from sa.arrival_time) = extract(day from ra.arrival_time)  "
            "order by route_id, hour asc; ")]
      (map (fn [[[route-id hour] maps]]
             [route-id hour (float (/ (apply + (map :diff maps)) (count maps)))])
           (group-by (fn [{:keys [hour route_id]}] [route_id hour]) results)))))

(defn passenger-counts-for-stop
  "Returns a seq of maps of passenger counts for a given stop. Keys aer :route_id, :est_load, :time_stop, and :hour"
  [stop-tag]
  (db/with-conn
    (sql/with-query-results results
      [(str "select route_id, est_load, "
            "cast(extract(hour from time_stop) as integer) as hour "
            "from passenger_count "
            "where stop_id = '" stop-tag "';")]
      (doall results))))
