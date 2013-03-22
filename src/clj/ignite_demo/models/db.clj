(ns ignite-demo.models.db
  (:use korma.db
        korma.core))

;; postgresql://user:password@host:port/db_name
(let [db-params (re-find #"postgresql://(\w+):([^@]+)@\w+:?[0-9]*/(\w+)"
                         (System/getenv "DATABASE_URL"))
      [_ user password db-name] db-params]
  (defdb db (postgres {:db db-name
                       :user user
                       :password password})))

(defmacro with-conn
  "Simple wrapper for sql/with-connection for the database specified by the enivornment variable DATABASE_URL"
  [& body]
  `(sql/with-connection (System/getenv "DATABASE_URL")
     ~@body))

(declare stop route direction direction_stop)

(defentity stop
  (has-many direction_stop))
(defentity route
  (has-many direction))
(defentity direction
  (belongs-to route)
  (has-many direction_stop))
(defentity direction_stop
  (belongs-to stop)
  (belongs-to direction))
