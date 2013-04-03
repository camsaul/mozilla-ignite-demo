(ns ignite-demo.models.route
  "Helper methods for getting and dealing with routes."
  (:use korma.db
        korma.core)
  (:require [clojure.java.jdbc :as sql]
            [ignite-demo.models.db :as db]
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
(defn route-tag-for-pcount-route-tag
  "The passenger count CSV data provided by the SFMATA uses different route numbers than what the routes are actu   ally called. This function translates the names used in passenger-count to what we use everywhere else."
  [pc-route-tag]
  (let [route-tag-as-int (try (Integer/parseInt pc-route-tag) (catch NumberFormatException e))]
    (if (or (nil? route-tag-as-int) (< route-tag-as-int 500))
      pc-route-tag
      (let [suffixes {5 "L", 6 " OWL", 7 "X", 8 "BX", 9 "AX" }
            hundreds (quot route-tag-as-int 100)
            prefix (rem route-tag-as-int 100)]
        (str prefix (suffixes hundreds))))))

(defn pcount-route-tag
  "The passenger count CSV provided by the SFMATA uses different route numbers than we use elsewhere. Converts a
   standard route tag to the special route tag used in the pcount CSV data."
  [route-tag]
  (let [prefixes {"L" 5, " OWL" 6, "X" 7, "BX" 8, "AX" 9}
        regexes (map #(re-pattern (str "([0-9]+)" %)) (keys prefixes))
        clauses (flatten (map (fn [regex prefix]
                                (let [result (re-find regex route-tag)]
                                  [(not (nil? result))
                                   (format "%d%02d" prefix
                                           (try (Integer/parseInt (last result)) (catch Exception e)))]))
                              regexes (vals prefixes)))]
    (eval `(cond ~@clauses :else ~route-tag))))

(defn passenger-counts-for-route
  "Returns a vector of [stop-id avg-pcount] vectors."
  [route-tag]
  (db/with-conn
    (sql/with-query-results results
      [(str "select stop_id, est_load "
            "from passenger_count "
            "where route_id = '" (pcount-route-tag route-tag) "';")]
      (mapv (fn [[stop-id s-map]]
              [stop-id (float (/ (apply + (map :est_load s-map)) (count s-map)))])
            (group-by :stop_id results)))))
