(defproject ignite_demo "0.1"
  :description "LuckyBird Inc. Mozilla Ignite Demo"
  :url "https://github.com/cammsaul/mozilla-ignite-demo"
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.2"]
                 [korma "0.3.0-RC4"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [postgresql "9.1-901.jdbc4"]
                 [org.clojure/clojurescript "0.0-1586"]]
  :plugins [[lein-ring "0.8.2"]
            [codox "0.6.4"]
            [lein-cljsbuild "0.3.0"]]
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/application.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :codox {:src-dir-uri "http://github.com/cammsaul/mozilla-ignite-demo/blob/master"
          :src-linenum-anchor-prefix "L"}
  :ring {:handler ignite-demo.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  :min-lein-version "2.0.0")
