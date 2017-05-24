(ns utilza.uuid
  (:import java.util.UUID)
  (:require 
   [clojure.string :as string]
   [clojure.test :as t]))


(defn uuids->short-url
  "Convert a UUID string to a short base36 string for use in URLs"
  [uuids]
  (-> uuids
      (.replace   "-" "")
      (BigInteger. 16)
      (.toString 36)))

(defn uuid->short-url
  "Convert a UUID to a short base36 string for use in URLs"
  [uuid]
  (-> uuid
      .toString
      uuids->short-url))


(defn short-url->uuid
  "Convert a short base36 string into a UUID."
  [short-url]
  (let [bi (BigInteger. short-url 36)]
    (-> bi
        (.shiftRight 64)
        .longValue
        (java.util.UUID. (.longValue bi)))))

(defn short-url->uuids
  "Convert a short base36 string into a UUID string."
  [short-url]
  (-> short-url
      short-url->uuid
      .toString))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TESTS


(t/deftest uuid-round-trip
  (let [u (java.util.UUID/randomUUID)
        s (uuid->short-url  u)
        ru (short-url->uuid s)]
    (t/is (= ru u))))
        

