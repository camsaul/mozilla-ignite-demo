(ns ignite-demo.models.route
  (:use korma.db
        korma.core)
  (:require [ignite-demo.models.db :as db]))

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
