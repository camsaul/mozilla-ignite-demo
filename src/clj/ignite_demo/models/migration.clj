(ns ignite-demo.models.migration
  "Handles creation and population of the database for the application."
  (:require [clojure.java.jdbc :as sql]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [ignite-demo.models.db :as db]
            [ignite-demo.models.utility :as util]
            [ignite-demo.models.route :as r])
  (:use korma.db
        korma.core))

(defn create-routes
  "Creates the routes table."
  []
  (db/with-conn
    (sql/create-table
     :route
     [:id :varchar "PRIMARY KEY NOT NULL"]
     [:title :varchar "NOT NULL"]
     [:latmin :float "NOT NULL"]
     [:latmax :float "NOT NULL"]
     [:lonmin :float "NOT NULL"]
     [:lonmax :float "NOT NULL"])))

(defn create-directions
  "Creates the directions table."
  []
  (db/with-conn
    (sql/create-table
     :direction
     [:id :varchar "PRIMARY KEY NOT NULL"]
     [:route_id :varchar "NOT NULL"]
     [:title :varchar "NOT NULL"]
     [:name :varchar "NOT NULL"])))

(defn create-stops
  "Creates the stops table."
  []
  (db/with-conn
    (sql/create-table
     :stop
     [:id :varchar "PRIMARY KEY NOT NULL"]
     [:title :varchar "NOT NULL"]
     [:lat :float "NOT NULL"]
     [:lon :float "NOT NULL"])))

(defn create-directions-stops
  "Creates the directions_stops table."
  []
  (db/with-conn
    (sql/create-table
     :direction_stop
     [:direction_id :varchar "NOT NULL"]
     [:stop_id :varchar "NOT NULL"])))

(defn create-realtime-arrival
  "Creates the realtime_arrival table."
  []
  (db/with-conn
    (sql/create-table
     :realtime_arrival
     [:route_id :varchar "NOT NULL"]
     [:stop_id :varchar "NOT NULL"]
     [:trip_id :integer "NOT NULL"]
     [:block_id :integer]
     [:arrival_time :timestamp "without time zone NOT NULL"])))

(defn create-scheduled-arrival
  "Creates the scheduled_arrival table."
  []
  (db/with-conn
   (sql/create-table
    :scheduled_arrival
    [:route_id :varchar "NOT NULL"]
    [:stop_id :varchar "NOT NULL"]
    [:trip_id :integer "NOT NULL"]
    [:block_id :integer]
    [:arrival_time :timestamp "WITHOUT TIME ZONE NOT NULL"])))

(defn create-passenger-count
  "Creates the passenger_count table."
  []
  (db/with-conn
    (sql/create-table
     :passenger_count
     [:route_id :varchar "NOT NULL"]
     [:stop_id :varchar "NOT NULL"]
     [:getting_on :integer]
     [:getting_off :integer]
     [:est_load :integer]
     [:trip_id :integer "NOT NULL"]
     [:dir :integer]
     [:time_stop :timestamp "WITHOUT TIME ZONE"]
     [:time_door_close :timestamp "WITHOUT TIME ZONE"]
     [:time_pull_out :timestamp "WITHOUT TIME ZONE"])))

(defn populate-scheduled-arrival  "Populated the scheduled_arrival table with data from a CSV file"
  [path-to-csv-file]
  (util/do-lines-in-csv-file
   [{:strs [PUBLIC_ROUTE_NAME, TRIP_ID, BLOCK_NAME, SCHEDULED_ARRIVAL_TIME,
            LATITUDE, LONGITUDE]} path-to-csv-file
            route-id (second (re-find #"0*(.*)" PUBLIC_ROUTE_NAME))
            BLOCK_NAME (if (empty? BLOCK_NAME) "NULL" BLOCK_NAME)
            [lat lon] (map #(Float/parseFloat %) [LATITUDE LONGITUDE])
            stop-id (:id (r/closest-stop-along-route route-id lat lon))]
   (if stop-id
     (db/with-conn
       (sql/do-prepared
        (format (str "INSERT INTO scheduled_arrival (route_id, stop_id, trip_id, block_id, arrival_time) "
                     "VALUES ('%s', '%s', %s, %s, '%s');")
                route-id, stop-id, TRIP_ID BLOCK_NAME, SCHEDULED_ARRIVAL_TIME))))))

(defn recreate-scheduled-arrival []
  (time (do (db/with-conn (sql/drop-table :scheduled_arrival))
            (create-scheduled-arrival)
            (populate-scheduled-arrival "C:/munidata/scheduled-arrivals.csv"))))

(defn populate-realtime-arrival
  "Populates the realtime_arrival table with CSV data"
  [path-to-csv-file]
  (util/do-lines-in-csv-file
   [{:strs [PUBLIC_ROUTE_NAME, STOP_ID, TRIP_ID, BLOCK_NAME, NEXTBUS_ARRIVAL_TIME]} path-to-csv-file
    BLOCK_NAME (if (empty? BLOCK_NAME) "NULL" BLOCK_NAME)
    route-id (second (re-find #"0*(.*)" PUBLIC_ROUTE_NAME))]
   (db/with-conn      
     (sql/do-prepared
      (format (str "INSERT INTO realtime_arrival (route_id, stop_id, trip_id, block_id, arrival_time) "
                   "VALUES ('%s', '%s', %s, %s, '%s');")
              route-id, STOP_ID, TRIP_ID, BLOCK_NAME, NEXTBUS_ARRIVAL_TIME)))))

(defn populate-passenger-count
  "Populates the passenger_count table with CSV data."
  [path-to-csv-file]
  (util/do-lines-in-csv-file
   [{:strs [STOP_ID, ON, OFF, LOAD, ROUTE, TRIP_ID, DIR, TIMESTOP,
            TIMEDOORCLOSE, TIMEPULLOUT]} path-to-csv-file
            [time-stop time-door-close time-pull-out]
            (map (fn [time-str]
                   (let [[_ hours rest-str] (re-find #"^([0-9]+)(:.*)" time-str)
                         hours-int (Integer/parseInt hours)]
                     (if (>= hours-int 24)
                       (str "10/2/12 " (- hours-int 24) rest-str)
                       (str "10/1/12 " time-str))))
                 [TIMESTOP TIMEDOORCLOSE TIMEPULLOUT])]
   (db/with-conn
     (sql/do-prepared
      (format (str "INSERT INTO passenger_count (route_id, stop_id, getting_on, getting_off, est_load, "
                   "trip_id, dir, time_stop, time_door_close, time_pull_out) "
                   "VALUES ('%s', '%s', %s, %s, %s, %s, %s, '%s', '%s', '%s');")
              ROUTE, STOP_ID, ON, OFF, LOAD, TRIP_ID, DIR, time-stop, time-door-close, time-pull-out)))))

(defn recreate-passenger-count []
  (time (do (db/with-conn (sql/drop-table :passenger_count))
            (create-passenger-count)
            (populate-passenger-count "C:/munidata/passenger-count.excerpt.csv"))))

(declare parse-direction parse-stop)

(defn parse-route
  "Parses the xml-zip for a route and inserts it into the database. Calls parse-direction and parse-stop on child direction and stop tags."
  [route]
  (let [{:keys [attrs content]} route
        {:keys [tag title latMin latMax lonMin lonMax]} attrs
        content (group-by :tag content)
        {:keys [stop direction path]} content]
    (println "Parsing route:" tag)
    ;; INSERT THE ROUTE
    (insert db/route
            (values [{:id tag 
                      :title title 
                      :latmin (Float/valueOf latMin)
                      :latmax (Float/valueOf latMax)
                      :lonmin (Float/valueOf lonMin)
                      :lonmax (Float/valueOf lonMax)}]))
    ;; PARSE THE DIRECTIONS
    (map (partial parse-direction tag) (:direction content))
    ;; PARSE THE STOPS
    (map parse-stop (:stop content))))

(defn parse-direction
  "Parses the xml-zip for a direction and inserts into the database. Parses child stop tags and inserts them into directions_stops."
  [route-tag direction]
  (let [{:keys [attrs content]} direction
        {:keys [tag title name]} attrs
        stops (map :tag (map :attrs content))]
    (println "Parsing direction:" tag)
    ;; INSERT THE DIRECTION
    (insert db/direction
            (values [{:route_id route-tag
                      :id tag
                      :title title
                      :name name}]))
    ;; INSERT STOPS INTO DIRECTIONS_STOPS
    (map #(insert db/direction_stop
                  (values [{:direction_id tag
                            :stop_id %}]))
         stops)))

(def parsed-stops (atom #{}))

(defn parse-stop
  "Parses the xml-zip for a stop and inserts it into the database if it is not already present."
  [stop]
  (let [{:keys [attrs]} stop
        {:keys [tag title lat lon]} attrs]
    (if-not (contains? @parsed-stops tag)
      (do
        ;; INSERT THE STOP
        (insert db/stop
                (values {:id tag
                         :title title
                         :lat (Float/valueOf lat)
                         :lon (Float/valueOf lon)}))
        (swap! parsed-stops conj tag))))) ; add stop to list of stops we've already seen

(defn -main
  "Creates the database structure and populates it."
  []
  (println "Creating database structure...")
  (flush)
  (create-routes)
  (create-stops)
  (create-directions)
  (create-directions-stops)
  (create-scheduled-arrival)
  (create-realtime-arrival)
  (create-passenger-count)
  (println "Done.")
  (println "Populating database (route, stop, direction, direction_stop)...")
  (let [routes-zip (-> "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni"
                       xml/parse
                       zip/xml-zip
                       first
                       :content)]
  (pmap parse-route routes-zip))
  (println "Populating database (realtime_arrival)...")
  (populate-realtime-arrival "C:/munidata/realtime-arrivals.csv")
  (println "Populating database (scheduled_arrival)...")
  (populate-scheduled-arrival "C:/munidata/scheduled-arrivals.csv")
  (println "Populating database (passenger_count)...")
  (populate-passenger-count "C:/munidata/passenger-count.csv")
  (println "Done."))

(defn drop-tables
  "Drops all of the tables in the database."
  []
  (db/with-conn
    (mapv sql/drop-table [:route :stop :direction :direction_stop :scheduled_arrival :realtime_arrival])))
