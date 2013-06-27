;; utilities for working with datomic

(ns utilza.datomic
  (:require [datomic.api :as d]
            [utilza.core :as cora])
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
  (doall
   (for [tx (-> fname
                clojure.java.io/reader 
                Util/readAll)]
     @(d/transact conn  tx))))


(defn auto-create
  "Create mem db's for testing, if the uri is a mem db. Dangerous in production!"
  [uri]
  (when (.contains uri "datomic:mem")
    (d/create-database uri)))




(defn re-seed
  "Load a seq of files worth of transactions into conn"
  [files conn]
  (doall
   (for [f files]
     (load-from-file f conn))))


(defn id-by-key
  "Finds an entity id by a pseudo key, or if not found, returns a new key."
  [m k part]
  (or (fq '[:find  ?e
               :in $ ?k ?v
               :where [?e ?k ?v]]
             k
             (k m))
      (d/tempid part)))

(defn map-and-id
  [m part fkey kmap]
  (let [m1 (cora/select-and-rename m kmap)]
    [(id-by-key m1 fkey part) m1]))