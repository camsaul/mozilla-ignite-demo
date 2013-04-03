(ns ignite-demo.views.layout
  "Provides a common HTML wrapper for all pages in the site"
  (:use [hiccup.page :only (html5 include-css include-js)])
  (:require [ignite-demo.models.route :as r]))

(declare header footer)

(defn page
  "Takes a page title and hiccup HMTL elements and wraps it with common stylesheet references etc."
  [title & body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title title]
    (include-css "/css/bootstrap.css"
                 "/css/flat-ui.css"
                 "/css/application.css")]
   [:body
    (apply conj [:div {:class "container"} (header)] body)
    (footer)
    (include-js "/js/jquery-1.8.2.min.js"
                "/js/jquery-ui-1.10.0.custom.min.js"
                "/js/jquery.dropkick-1.0.0.js"
                "/js/custom_checkbox_and_radio.js"
                "/js/custom_radio.js"
                "/js/jquery.tagsinput.js"
                "/js/bootstrap-tooltip.js"
                "/js/jquery.placeholder.js"
                "/js/application.js")]))

(defn header
  "Returns a common HTML header for pages in the site."
  []
  [:div {:class "row"}
   [:span {:class "span9"}
    [:div {:class "navbar"}
     [:div {:class "navbar-inner"}
      [:div {:class "container"}
       [:div {:class "nav-collapse collapse"}
        [:ul {:class "nav"}
         [:li [:a {:href "/"} "Select Route"]
          [:ul
           (map (fn [route]
                  (vector :li [:a {:href (str "/route/" (:id route))}
                               (:title route)]))
                (r/all))]]
         [:li [:a {:href "http://getluckybird.com"} "LuckyBird, Inc."]]]]]]]]])

(defn footer
  "Returns a common HTML footer for pages in the site."
  []
  [:footer
   [:div {:class "container"}
    [:div {:class "row"}
     [:h3 {:class "footer-title"} "2013 LuckyBird, Inc."]]]])
