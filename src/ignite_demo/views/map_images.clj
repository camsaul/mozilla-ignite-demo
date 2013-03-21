(ns ignite-demo.views.map-images
  "Contains helper functions to generate google static map API images")

(declare path-params-for-stops markers-params-for-stops)

(def google-maps-api-key (System/getenv "GOOGLE_MAPS_API_KEY"))

(defn map-image-for-stops
  "Returns the Google static map API image (map-size pixels square) for a given ordered seq of stops."
  [map-size stops]
  [:img {:height map-size
         :width map-size
         :src
         (str "http://maps.googleapis.com/maps/api/staticmap"
              "?size=" map-size "x" map-size
              "&maptype=roadmap"
              (path-params-for-stops stops)
              "&key=" google-maps-api-key
              "&sensor=false")}])

(defn path-params-for-stops
  "Takes a seq of stops and returns the &path=... param string to be used with the Google Maps Static Map API."
  [stops]
  (apply str "&path=color:0x0000ff|weight:5"
         (map (fn [stop]
                (str "|" (:lat stop) "," (:lon stop)))
              stops)))

(defn markers-params-for-stops
  "Takes a seq if stops and returns the &markers... param string to be used with the Google Maps Static Map API"
  [stops]
  (apply str
         (map (fn [stop]
                (str "&markers=color:blue|label:|" (:lat stop) "," (:lon stop)))
              stops)))

(defn map-image-for-stop
  "Returns an IMG element of size (square) for the stop at given lat and lon. You may optionally specify the zoom level for the map (default is 18)"
  ([lat lon size]
     (map-image-for-stop lat lon size 18))
  ([lat lon size zoom]
     [:img {:height size
            :width size
            :src
            (str "http://maps.googleapis.com/maps/api/staticmap"
                 "?center=" lat "," lon
                 "&zoom=" zoom
                 "&size=" size "x" size
                 "&maptype=roadmap"
                 "&markers=color:blue%7Clabel:%7C" lat "," lon
                 "&key=" google-maps-api-key
                 "&sensor=false")}]))
