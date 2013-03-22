(ns ignite-demo.core
  "The core namespace for the ClojureScript portion of the project. Handle jQuery $(document).ready functionality")

(.ready (js/$ js/document)
        (fn []
          (.click (js/$ ".todo li")
                  (fn []
                    (.toggleClass (js/$ js/this) "todo-done")))
          (.tooltip (js/$ "[data-toggle=tooltip]"))
          (.popover (js/$ "[data-toggle=popover]"))))
