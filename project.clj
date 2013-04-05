(defproject ignite_demo "1.0"
  :description "LuckyBird Inc. Mozilla Ignite Demo"
  :url "https://github.com/cammsaul/mozilla-ignite-demo"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/math.numeric-tower "0.0.2"] ; formerly clojure.contrib.math
                 [org.clojure/clojurescript "0.0-1586"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.2"]
                 [korma "0.3.0-RC4"]
                 [postgresql "9.1-901.jdbc4"]
                 [clojure-csv/clojure-csv "2.0.0-alpha1"]
                 [jayq "2.3.0"]
                 [amalloy/ring-gzip-middleware "0.1.2"]]
  :plugins [[lein-ring "0.8.2"]
            [codox "0.6.4"]
            [lein-cljsbuild "0.3.0"]]
  :jvm-opts ["-Xmx1024m"]
  :source-paths ["src/clj" "src/cljs"]
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/application.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :externs ["externs/jquery-1.8.js" "externs/externs.js"]}}]}
  :codox {:sources ["src/clj" "src/cljs"]
          :src-dir-uri "http://github.com/cammsaul/mozilla-ignite-demo/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :ring {:handler ignite-demo.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  :min-lein-version "2.0.0")
