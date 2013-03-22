(ns ignite-demo.models.migration
  "Handles creation and population of the database for the application."
  (:require [clojure.java.jdbc :as sql]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [ignite-demo.models.db :as db])
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

(defonce routes-zip
  (-> "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni"
      xml/parse
      zip/xml-zip
      first
      :content))

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
    (map parse-stop (:stop content))
    ))

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
  (println "Done.")
  (println "Populating database...")
  (pmap parse-route routes-zip)
  (println "Done."))

(defn drop-tables
  "Drops all of the tables in the database."
  []
  (db/with-conn
    (mapv sql/drop-table [:route :stop :direction :direction_stop])))
