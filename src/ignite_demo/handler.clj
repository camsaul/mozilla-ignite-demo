(ns ignite-demo.handler
  "The main handler for requests."
  (:use compojure.core
        [hiccup.page :only (html5)]
        korma.db
        korma.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ignite-demo.views.route :as r]
            [ignite-demo.views.index :as index]
            [ignite-demo.views.stop :as s]))

(defroutes app-routes
  (GET "/" [] (index/display))
  (GET "/route/:id" [id] (r/display id))
  (GET "/stop/:id" [id] (s/display id))
  (route/resources "/")
  (route/not-found "Not found."))

(def app
  (handler/site app-routes))
