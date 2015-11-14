(ns utilza.json
  (:require 
   [cheshire.core :as json]
   [cheshire.generate :as jgen]
   [clj-time.coerce :as coerce]
   [utilza.repl :as urepl]
   [clojure.pprint :as pprint])
  (:import java.util.Date
           org.joda.time.DateTime))


(defn decode
  "Convenience function so I don't have to (#(decode % true)) in ->> chains"
  [s]
  (json/decode s true))

(defn massive-json->edn
  "Convert a massive JSON tree into pretty-printed readable EDN"
  [json-file-name edn-file-name]
  (-> json-file-name 
      slurp 
      (json/decode true)
      (#(urepl/massive-spew edn-file-name %))))


(defn serialize-date
  [c jg]
  (-> c
      org.joda.time.DateTime.
      ;; TODO; use simpler-serialize date, adn eliminate dependncy on clj-time.coerce
      coerce/to-long
      (jgen/encode-long jg)))


(defn simpler-serialize-date
  [c jg]
  (-> c
      .getTime
      (jgen/encode-long jg)))


(defn json-response
  "Takes some clojure data, encodes it as JSON, wraps it in a ring response,
  adds the right header, and returns it."
  [data]
  {:status 200
   :headers {"Content-Type" "application/json;charset=UTF-8"
             ;; TODO: yeah, OK, maybe ETags might be useful in the future.
             "Pragma" "no-cache"
             "Expires" "Wed, 11 Jan 1984 05:00:00 GMT"
             "Cache-Control" "private, no-cache, no-store"}
   :body (json/encode data true)})


(defn slurp-json
  [f]
  (-> f
      slurp
      (json/decode true)))
