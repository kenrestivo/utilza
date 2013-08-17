(ns utilza.uuid-test
  (:use clojure.test
        utilza.uuid))

(deftest uuids
  (testing "Random UUID roundtrip")

  (let [uuid  (java.util.UUID/randomUUID)
        uuids (.toString uuid)]
    (is (=  uuids
            (-> uuids
                uuids->short-url
                short-url->uuids))))

  (testing "large value UUID roundtrip")
  (let [uuids "bd711f41-d433-4a2a-9afd-ba2cc855542b"]
    (is (=  uuids
            (-> uuids
                uuids->short-url
                short-url->uuids)))))