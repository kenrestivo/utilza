(ns utilza.json
  (:require 
   [cheshire.core :as json]
   [utilza.repl :as urepl]
   [clojure.pprint :as pprint]))

(defn massive-json->edn
  "Convert a massive JSON tree into pretty-printed readable EDN"
  [json-file-name edn-file-name]
  (-> json-file-name 
      slurp 
      (json/decode true)
      (#(urepl/massive-spew edn-file-name %))))