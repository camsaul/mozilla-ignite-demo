(ns ignite-demo.core
  "The core namespace for the ClojureScript portion of the project."
  (:use [jayq.core :only [$ document-ready bind toggle-class hide show attr]]
        [jayq.util :only [log]]))

(document-ready
 (fn []
   (bind ($ :li.todo) :click
         (fn []
           (toggle-class ($ :this) :todo-done)))
   (.tooltip ($ "[data-toggle=tooltip]"))
   (let [route-buttons ($ "div#passenger-count button.btn")
         route-tables ($ :div.route-table)]
     (mapv hide (rest route-tables))
     (mapv (fn [button]
             (let [route-id (attr button :route)]
               (bind button :click
                     (fn []
                       (mapv (fn [route-table]
                               (if (= (attr route-table :route) route-id)
                                 (show route-table)
                                 (hide route-table)))
                             route-tables)))))
           route-buttons))))
