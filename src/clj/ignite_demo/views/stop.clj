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

(declare display-time-tables display-passenger-counts)

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
              routes)]
        [:div
         [:h3 "Passenger counts:"]
         (display-passenger-counts stop-tag)]]]
      [:div {:class "span5"}
       (map-image-for-stop lat lon map-size)
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

(defn display-passenger-counts
  "Generates the HTML for show the passenger counts table for a stop."
  [stop-tag]
  (let [sdf (java.text.SimpleDateFormat. "KK:mm a")
        count-maps (sort-by :time_stop (s/passenger-counts-for-stop stop-tag))]
    `[:table {:style "margin-top: 10px;" :border 1 :cellpadding 10}
      [:tr ~@(map #(vector :th %) ["Route" "Time" "Getting On" "Getting Off" "Est. Passengers"])]
      ~@(map (fn [count-map]
               (let [{:keys [route_id getting_on getting_off est_load time_stop]} count-map
                     regular-route-tag (r/regular-route-tag-for-passenger-count-route-tag route_id)
                     formatted-time (.format sdf time_stop)]
                 `[:tr
                   ~@(map #(vector :td %) [regular-route-tag formatted-time getting_on getting_off est_load])]))
             count-maps)]))
