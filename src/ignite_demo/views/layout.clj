(ns ignite-demo.views.layout
  "Provides a common HTML wrapper for all pages in the site"
  (:use [hiccup.page :only (html5 include-css)]))

(defn page
  "Takes a page title and hiccup HMTL elements and creates a"
  [title & body]
  (html5
   [:head
    [:title title]
    (include-css "/stylesheets/base.css")]
   [:body
    (apply conj [:div {:id "wrapper"}] body)]))
