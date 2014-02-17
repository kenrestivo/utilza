(ns utilza.json
  (:require 
   [cheshire.core :as json]
   [cheshire.generate :as jgen]
   [clj-time.coerce :as coerce]
   [utilza.repl :as urepl]
   [clojure.pprint :as pprint])
  (:import java.util.Date
           org.joda.time.DateTime))


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
      coerce/to-long
      (jgen/encode-long jg)))