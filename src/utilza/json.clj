(ns utilza.json
  (:require 
   [cheshire.core :as json]
   [clojure.pprint :as pprint]))

(defn massive-json->edn
  "Convert a massive JSON tree into pretty-printed readable EDN"
  [json-file-name edn-file-name]
  (binding [*print-length* 10000 *print-level* 10000]
    (as-> json-file-name <>
          (slurp <>)
          (json/decode <> true)
          (with-out-str (pprint/pprint <>))
          (spit edn-file-name <>))))