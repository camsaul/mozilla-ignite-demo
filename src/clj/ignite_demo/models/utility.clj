(ns ignite-demo.models.utility
  (:require [clojure-csv.core :as csv]
            [clojure.java.io :as io]))

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

(def csv-line-num (atom 0))
(defmacro do-lines-in-csv-file
  "Parses every line in a csv file in parallel and creates a map keyed by the values of each line. /
Successively binds each line to line-binding and executes body via dorun (presumably for side effects). /
Returns nil. Additional bindings (evaluated after each line is bound) may also be specified. "
  [[line-binding path-to-csv-file & other-bindings] & body]
  (reset! csv-line-num 0)
  `(with-open [reader# (io/reader ~path-to-csv-file)]
     (let [lines# (line-seq reader#)
           [keys#] (csv/parse-csv (first lines#))
           parsed-lines# (pmap #(zipmap keys# (first (csv/parse-csv %)))(rest lines#))]
       (dorun
        (pmap (fn [~line-binding]
                (swap! csv-line-num inc)
                (let [line-num# @csv-line-num]
                  (if (= 0 (rem line-num# 500))
                    (println "CSV line num: " @csv-line-num)))
                (let [~@other-bindings]
                  ~@body))
              parsed-lines#)))))

(defn format-time
  "Takes an hour (0-23) and an optional minute and returns a pretty string such as 8PM or 8:30PM"
  ([hour]
     (cond (zero? hour) "12AM"
           (= 12 hour) "12PM"
           (> hour 12) (str (- hour 12) "PM")
           :else (str hour "AM")))
  ([hour minute]
     (cond (zero? hour) (format "12:%02dAM" minute)
           (= 12 hour) (format "12:%02dPM" minute)
           (> hour 12) (format "%d:%02dPM" (- hour 12) minute)
           :else (format "%d:%02dAM" hour minute))))
