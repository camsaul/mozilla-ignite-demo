(ns ignite-demo.core
  "The core namespace for the ClojureScript portion of the project."
  (:use [jayq.core :only [$ document-ready bind toggle-class hide show attr]]
        [jayq.util :only [log]]))

(defn hide-if-not-route-id
  "Hides element if it does not have a route attribute equal to route-id"
  [element route-id]
  (if (not= (attr ($ element) :route) route-id)
      (hide element)
      (show element)))

(document-ready
 (fn []
   (bind ($ :li.todo) :click
         (fn []
           (toggle-class ($ :this) :todo-done)))
   (.tooltip ($ "[data-toggle=tooltip]"))
   (let [route-buttons ($ "div#passenger-count button.btn")
         p-charts ($ :div.route-table)
         a-charts ($ :div.arrivals-chart)
         default-route-id (attr ($ (first route-buttons)) :route)         
         hide-all-not-id (fn [id] (mapv (fn [charts]
                                          (mapv #(hide-if-not-route-id % id) charts))
                                        [p-charts a-charts]))]
     (hide-all-not-id default-route-id)
     (mapv (fn [button]
             (let [route-id (attr ($ button) :route)]
               (bind button :click #(hide-all-not-id route-id))))
           route-buttons))))
