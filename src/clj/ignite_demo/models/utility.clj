(ns ignite-demo.models.utility)

(defn distance-between-coordinates
  "Returns the distance in meters between two locations specified by their lats and lons."
  [^double lat1 ^double lon1 ^double lat2 ^double lon2]
  (let [RADIUS 6371000
        RAD-PER-DEG 0.017453293
        lat1 (* lat1 RAD-PER-DEG)
        lon1 (* lon1 RAD-PER-DEG)
        lat2 (* lat2 RAD-PER-DEG)
        lon2 (* lon2 RAD-PER-DEG)
        dlat (- lat2 lat1)
        dlon (- lon2 lon1)
        a (+ (* (Math/cos lat1) (Math/cos lat2) (Math/pow (Math/sin (/ dlon 2)) 2))
             (Math/pow (Math/sin (/ dlat 2)) 2))
        c (* 2 (Math/atan2 (Math/sqrt a) (Math/sqrt (- 1 a))))
        d (* RADIUS c)]
    d))
