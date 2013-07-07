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
  [db m k part]
  {:pre [(-> k nil? not)
         (map? m)]}
  (or (ffirst (d/q '[:find  ?e
                     :in $ ?k ?v
                     :where [?e ?k ?v]]
                   db
                   k
                   (k m)))
      (d/tempid part)))


(defn map-and-id
  [m part fkey kmap]
  (let [m1 (cora/select-and-rename m kmap)]
    [(id-by-key m1 fkey part) m1]))




(defn select-rename-find
  "Takes a map, an external key fkey, and a keymap of transforms {:from-key :to-key}.
  Returns the map filtered to only include keys in keymap, with either a
  tempid or the id of whatever was found using fkey."
  [db part m fkey kmap]
  (let [m1 (cora/select-and-rename m kmap)]
    (assoc m1 :db/id (id-by-key db m1 fkey part))))




(defn find-by-key
  "Gets all entity ID's which have key k"
  [db k]
  {:pre [(-> k nil? not)]}
  (d/q '[:find  ?e
         :in $ ?k 
         :where [?e ?k  _]]
       db
       k))


(defn one-kv
  "Gets one and only one entity id for given key and value"
  [db k v]
  {:pre [(-> k nil? not)]}
  (ffirst
   (d/q '[:find ?e
         :in $ ?k ?v
         :where [?e ?k ?v]]
       db
       k
       v)))




