(ns ignite-demo.views.stop
  "Generates HTML for a stop page."
  (:use [hiccup.page :only (html5 include-css)]
        [hiccup.core :only (html)]
        korma.core
        korma.db
        ignite-demo.views.map-images)
  (:require [ignite-demo.models.stop :as s]
            [ignite-demo.models.route :as r]
            [ignite-demo.models.direction :as d]
            [ignite-demo.views.layout :as layout]
            [clojure.math.numeric-tower :as math]))

(declare display-time-tables display-passenger-counts display-passenger-counts-graph)

(defn display
  "Generates the HTML for a given stop-tag."
  [stop-tag]
  (let [the-stop (s/stop-for-tag stop-tag)
        routes (s/routes-that-serve-stop stop-tag)
        {:keys [title lat lon]} the-stop
        map-size 380]
    (layout/page title
     [:h1 title]
     [:div {:class "row"}
      [:div {:class "span5"}
       [:p (str "Coordinates: " lat ", " lon)]
       [:p (str "Stop ID: " stop-tag)]
       [:div
        [:h3 "Routes that Serve this stop:"]
        [:ul 
         (map (fn [route]
                (let [direction (first (r/directions-for-route (:id route)))
                      stops (d/stops-for-direction (:id direction))]
                  (vector :li [:a {:href (str "/route/" (:id route))
                                   :data-toggle "tooltip"
                                   :data-placement "right"
                                   :data-html "true"
                                   :data-original-title (html (map-image-for-stops 175 stops))}
                               (:title route)])))
              routes)]]]
      [:div {:class "span5"}
       (map-image-for-stop lat lon map-size)
       ]]
     [:div.row
      (display-passenger-counts stop-tag)]
     [:div.row
      [:div.span10
       (display-time-tables stop-tag)]])))

(defn display-time-tables
  "Generates the HTML for the scheduled/actual arrivals table for a stop."
  [stop-tag]
  (let [sdf (java.text.SimpleDateFormat. "E dd/MM KK:mm a")
        arrival-times (s/arrival-times-for-stop stop-tag)]
    (if (empty? arrival-times)
      [:p {:style "margin-top: 10px;"}
       "No arrival time data available for this stop."]
      `[:table {:border 1, :cellpadding 10, :style "margin-top: 10px;"}
        [:tr [:th "Route"] [:th "Scheduled"] [:th "Actual"]]
        ~@(map (fn [time]
                 (let [{:keys [route_id arrival_time diff]} time
                       late-str (if (< diff 0) "late" "early")
                       [mins-late secs-late] (map #(biginteger (math/abs(% diff 60))) [quot rem])]
                   `[:tr ~@(map #(vector :td %) [route_id (.format sdf arrival_time)
                                                 (format "%d:%02d %s" mins-late secs-late late-str)])]))
               arrival-times)])))

(defn display-passenger-counts-label
  "Helper method to display the label for a column on the passenger counts graph. Hour is an in 0-23 and pcount
   is a float for average passenger count during that hour."
  [hour pcount x-offset y-offset col-width]
  (let [am-pm (if (> hour 11) "PM" "AM")
        hour (if (> hour 12) (- hour 12) hour)
        pcount (int (Math/round pcount))
        pcount-str (if (< col-width 100) pcount (str pcount " passengers"))]
    [:text {:x x-offset :y (- y-offset 20)} (format "%s%s" hour am-pm)
     [:tspan {:x x-offset :y (- y-offset 5)} pcount-str]]))

(defn display-passenger-counts-graph
  "Generates an HTML 5 graph to show the passenger counts for a stop. Counts should be a vector of [hour 
   avg-pcount] pairs. "
  [route-tag hours-pcounts]
  (let [num-cols (count hours-pcounts)
        graph-width 780
        graph-height 400
        col-width (quot graph-width num-cols)
        x-offsets (iterate (partial + col-width) 0)
        max-pcount (apply max (map #(nth % 1) hours-pcounts))
        scale-y (quot (- graph-height 40) max-pcount)] ; leave a little padding at top for lables
    [:div.route-table {:route route-tag}
     [:b (str "Route " route-tag)]
     `[:svg {:xmlns "http://www.w3.org/2000/svg"
             :preserveAspectRatio "xMinYMin meet"
             :width ~graph-width :height ~graph-height :viewBox ~(format "0 0 %d %d" graph-width graph-height)}
       ~@(mapv (fn [[hour pcount] x-offset]
                 (let [[r g b] (repeatedly #(int (rand 255)))
                       height (* pcount scale-y)
                       y-offset (- graph-height height)]
                   [:g
                    [:rect {:x x-offset :y y-offset :width col-width :height height
                            :style (format "fill:rgb(%d,%d,%d);stroke-width:0;" r g b)} ""]
                    (display-passenger-counts-label hour pcount x-offset y-offset col-width)]))
               hours-pcounts x-offsets)]]))

(defn process-passenger-counts
  "Helper method to do preprocessing of passenger counts for display-passenger-counts. Returns a vector of
   [route [hour avg-est-load]+] vectors sorted by hour."
  [stop-tag]
  (let [all-maps (group-by :route_id
                           (map #(update-in % [:route_id] r/regular-route-tag-for-passenger-count-route-tag)
                                (s/passenger-counts-for-stop stop-tag)))]
    (mapv (fn [[route maps]]
            (let [all-hour-maps (group-by :hour (sort-by :hour maps))]
              `[~route ~@(mapv (fn [[hour hour-maps]]
                                [hour (float (/ (reduce + (map :est_load hour-maps)) (count hour-maps)))])
                              all-hour-maps)]))
          all-maps)))

(defn display-passenger-counts
  "Generates the HTML for show the passenger counts table for a stop."
  [stop-tag]
  (let [pcounts (process-passenger-counts stop-tag)]
    `[:div#passenger-count.span10
      [:h3 "Passenger counts:"]
      [:div.btn-group ~@(mapv (fn [[route-tag _]] [:button.btn {:route route-tag} route-tag])
                              pcounts)]
      ~@(mapv (fn [[route-tag & counts]]
                (display-passenger-counts-graph route-tag counts))
              pcounts)]))
