(ns ignite-demo.views.index
  "View for the index page."
  (:use [hiccup.page :only (html5 include-css)]
        korma.db
        korma.core
        ignite-demo.models.db))

(defentity muni-routes
  (table :route :muni-routes))

(def routes-list
  (select muni-routes
          (fields :id :title)))

(defn display
  "Generates the HTML for the index page."
  []
  (html5 [:head
          (include-css "/stylesheets/base.css")]
         [:body
          [:div {:id "wrapper"}
           [:h3 "ROUTES"]
           [:ul
            (map #(vector
                   :li
                   [:a {:href (str "route/" (:id %))}
                    (:title %)])
                 routes-list)]]]))
