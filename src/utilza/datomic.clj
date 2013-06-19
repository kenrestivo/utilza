;; utilities for working with datomic

(ns utilza.datomic
  (:require [datomic.api :as d])
  (:import datomic.Util))




(defn fq
  "Loads the first result"
  [& args]
  (->> args
       (apply d/q)
       ffirst))


(defn load-from-file
  "Load and run bunch of transactions from a file"
  [fname conn]
  (doseq [tx (-> fname
                 clojure.java.io/reader 
                 Util/readAll)]
    (d/transact conn  tx)))


(defn auto-create
  "Create mem db's for testing, if the uri is a mem db. Dangerous in production!"
  [uri]
  (when (.contains uri "datomic:mem")
    (d/create-database uri)))




(defn re-seed
  "Load a seq of files worth of transactions into conn"
  [files conn]
  (doseq [f files]
    (load-from-file f conn)))
