(ns utilza.uuid-test
  (:use clojure.test
        utilza.uuid))

(deftest uuids
  (testing "UUID roundtrip")

  (let [uuid  (java.util.UUID/randomUUID)
        uuids (.toString uuid)]
    (is (=  uuids
            (-> uuids
                uuids->short-url
                short-url->uuids)))))