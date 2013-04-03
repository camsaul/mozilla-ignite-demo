(ns ignite-demo.handler
  "The main handler for requests."
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ignite-demo.views.route :as r]
            [ignite-demo.views.index :as index]
            [ignite-demo.views.stop :as s]
            [ignite-demo.models.stop :as stop]
            [ignite-demo.models.route :as rt]
            [ring.middleware.gzip :as gzip]))

(defn with-time
  "Helper function to print the time it takes to handle a request to the server logs"
  [display-fn]
  (fn [& args]
    (time (apply display-fn args))))

(def display-index (memoize index/display))
(def display-stop (memoize s/display))
(def display-route (memoize r/display))

(defroutes app-routes
  "The various routes for the app."
  (GET "/" [] ((with-time display-index)))
  (GET "/route/:id" [id] ((with-time display-route) id))
  (GET "/stop/:id" [id] ((with-time display-stop) id))
  (route/resources "/")
  (route/not-found "Not found."))

(def app
  (-> (handler/site app-routes)
      (gzip/wrap-gzip)))

(defn memoize-all
  "Memoizes all pages for stops and routes to improve performance."
  []
  (println "Starting memoization...")
  (print "index:")
  (time (dorun (display-index)))
  (println "routes:")
  (time (dorun (pmap #(display-route (:id %)) (rt/all))))
  (println "stops:")
  (time (dorun (pmap #(display-stop (:id %)) stop/all-stops))))

(memoize-all)

