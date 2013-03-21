(ns ignite-demo.models.direction
  (:use korma.db
        korma.core)
  (:require [ignite-demo.models.db :as db]))

(defn stops-for-direction
  "Returns a seq of all stops for a given direction id"
  [direction-tag]
  (select db/direction_stop
          (where {:direction_id direction-tag})
          (fields) ; don't return any of the direction_stop fields
          (with db/stop)))
