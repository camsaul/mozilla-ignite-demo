(ns ignite-demo.handler
  "The main handler for requests."
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ignite-demo.views.route :as r]
            [ignite-demo.views.index :as index]
            [ignite-demo.views.stop :as s]
            [ignite-demo.models.stop :as stop]))

(defn with-gzip [handler]
  "Helper to gzip data if the client accepts it"
  (fn [request]
    (let [response (handler request)
          out (java.io.ByteArrayOutputStream.)
          accept-encoding (.get (:headers request) "accept-encoding")]

      (if (and (not (instance? java.io.File (:body response)))
               (not (nil? accept-encoding))
               (re-find #"gzip" accept-encoding))
        (do
          (doto (java.io.BufferedOutputStream.
                 (java.util.zip.GZIPOutputStream. out))
            (.write (.getBytes (:body response)))
            (.close))

          {:status (:status response)
           :headers (assoc (:headers response)
                      "Content-Type" "text/html"
                      "Content-Encoding" "gzip")
           :body (java.io.ByteArrayInputStream. (.toByteArray out))})
        response))))

(def display-index (memoize index/display))
(def display-stop (memoize s/display))

(defroutes app-routes
  (GET "/" [] (display-index))
  (GET "/route/:id" [id] (r/display id))
  (GET "/stop/:id" [id] (display-stop id))
  (route/resources "/")
  (route/not-found "Not found."))

(def app
  (handler/site (with-gzip app-routes)))

(defn memoize-all
  "Memoizes all pages for stops and routes to improve performance."
  []
  (time (dorun (pmap #(display-stop (:id %)) stop/all-stops))))
(memoize-all)
