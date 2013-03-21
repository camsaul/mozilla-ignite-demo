(ns ignite-demo.views.index
  "View for the index page."
  (:use [hiccup.page :only (html5 include-css)]
        korma.db
        korma.core)
  (:require [ignite-demo.models.db :as db]
            [ignite-demo.views.layout :as layout]))

(def routes-list
  (select db/route
          (fields :id :title)))

(defn display
  "Generates the HTML for the index page."
  []
  (layout/page "Routes List"
   [:h3 "ROUTES"]
   [:ul
    (map #(vector
           :li
           [:a {:href (str "route/" (:id %))}
            (:title %)])
         routes-list)]))
