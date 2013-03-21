(ns ignite-demo.models.test.utility
  (:use clojure.test
        ignite-demo.models.utility))

(deftest test-distance-between-coordinates
  (testing "distance between coordinates"
    ;; Distance between SF and LA should be 559 km.
    (is (= 559 (int (/ (distance-between-coordinates 37.783333  -122.416667 34.05 -118.25) 1000))))))
