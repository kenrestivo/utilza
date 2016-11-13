;; utilities for working with datomic

(ns utilza.datomic
  (:require [datomic.api :as d]
            [taoensso.timbre :as log]
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
  "Searches for an entity id where the key-value pair is present.
   Returns that, or a tempid in part if not found"
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




(defn find-by-index
  "Gets all indexed entity id's which have key k"
  [db k]
  {:pre [(-> k nil? not)]}
  (->> k
       (d/datoms db :avet)
       (map :e)))


(defn find-by-key
  "Gets all entity ID's which have key k"
  [db k]
  {:pre [(-> k nil? not)]}
  (d/q '[:find  ?e
         :in $ ?k 
         :where [?e ?k  _]]
       db
       k))


(defn count-by-key
  "Gets count of all entity ID's which have key k"
  [db k]
  {:pre [(-> k nil? not)]}
  (->>  (d/q '[:find  (count ?e)
               :in $ ?k 
               :where [?e ?k  _]]
             db
             k)
        ffirst))


(defn count-by-index
  "Gets count of all entity ID's which have key k"
  [db k]
  {:pre [(-> k nil? not)]}
  (->> k
       (d/datoms db :avet)
       seq
       count))

(defn one-kv
  "Gets one and only one entity id for given key and value.
   For attributes that are indexed, use one-index instead, it's faster."
  [db k v]
  {:pre [(-> k nil? not)]}
  (ffirst
   (d/q '[:find ?e
          :in $ ?k ?v
          :where [?e ?k ?v]]
        db
        k
        v)))


(defn not-set?
  "Equivalent to IS NULL in SQL. Very useful. Thanks to tomjack on IRC."
  [db e a]
  (nil? (seq (d/datoms db :eavt e a))))


(defn get-ea
  "Takes db, an alternate value, entity id, and attribute.
   Searches datoms for that entity with that attribute.
   Returns the value of that datom, or the alternate value (nil, 0, etc) if not present.
   This is an equivalent to an (IF NULL) in SQL."
  [db alt e a]
  (if-let [res (->> (d/datoms db :eavt e a) first :v)]
    res
    alt))



(defn one-index
  "Utility to obtain an eid for an indexed key k and value v."
  [db k v]
  (->> v (d/datoms db :avet k) first :e))

(defn unique-or-temp
  "Utility to obtain an eid for a synthetic unique key, or generate a tempid if it's not there."
  [db partition k v]
  ;; TODO: throw if > 1 count?
  (if-let [e (one-index db k v)]
    e
    (d/tempid partition)))



(defn ssquuid
  "Returns a string representation of a squuid"
  []
  (str (d/squuid)))


(defn add-uuid
  [db key-to-check key-to-add f]
  (for [id (d/q '[:find  ?u
                  :in $ ?c ?a
                  :where
                  [?u ?c _]
                  [(utilza.datomic/not-set? $ ?u ?a)]
                  ]
                db
                key-to-check
                key-to-add)]
    [:db/add (first id) key-to-add (f)]))
(defn add-ssquuid
  [db key-to-check key-to-add]
  (add-uuid db key-to-add key-to-add ssquuid))


(defn add-squuid
  [db key-to-check key-to-add]
  (add-uuid db key-to-add key-to-add d/squuid))

(defn convert-uuid
  "Convert a string UUID to an actual UUID"
  [db from-key to-key]
  (for [id (d/q '[:find ?u ?v
                  :in $ ?fk ?tk
                  :where
                  [?u ?fk ?v]
                  [(utilza.datomic/not-set? $ ?u ?tk)]
                  ]
                db
                from-key
                to-key)]
    [[:db/add (first id) to-key (java.util.UUID/fromString (second id))]
     [:db/retract (first id) from-key (second id)]]))

(defn exists?
  "Returns true if the eid exists in the db"
  [db eid]
  (not (empty? (d/q '[:find ?e :in $ ?e :where [?e]] db eid))))



(defn all-kv
  "Gets the entity and all its attributes, from a k an v."
  [db k v]
  (->> (one-kv db k v)
       (d/entity db)
       d/touch))


(defn child-without-loop
  "Takes a key for finding the parent, and an entity (map).
   Recurses up parents through the directed graph.
   Will stop and log an error if a cycle is detected, and return the graph up to that point.
   Returns a seq of the child entities in order they were traversed (bottom to top)."
  [k e]
  (loop [m e
         result [e]]
    (let [child (k m)
          cycle-detected? (some #(= % (:db/id child)) (map :db/id result))]
      (when cycle-detected?
        ;; XXX Yuck for having a log dependency in here,
        ;; but I don't want to throw an error, since the whole point is to strip
        ;; cycles to the incoming entity cycle-safe, not to just detect them and throw.
        (log/error (apply format "Cycle detected! orig map: %s, map %s, child %s, accumulator %s "
                          (map pr-str [e m child result]))))
      (if (or (nil? child) cycle-detected?)
        result
        (recur child
               (conj result child))))))


(defn child-ancestry
  "Utility function used in datomic queries to return a seq of a child's eid path to its ultimate parent.
   Takes a database and a child's eid."
  [db k eid]
  (->> eid
       (d/entity db)
       ;; TODO: don't use iterate, have to look for duplicates which would be a loop.
       (child-without-loop k)
       rest ;; the first one is itself
       (map :db/id)))


(defn child-depth
  "Utility function used in datomic queries to count the depth of a child.
   Takes a database and a child's eid.
   Returns the count of levels for that child."
  [db k eid]
  (->> eid
       (child-ancestry db k)
       count))



(defn merge-duplicates
  "Takes a db handle, and a list of EID's, master EID first.
   Generates transactions tha merge all eid's attributes into the first EID,
   qand retracts all the other eids"
  [db eids]
  (let [[keep & undo] eids]
    (vector (for [u undo]
              [:db.fn/retractEntity u])
            (concat (->> (concat (for [ds (map (partial d/datoms db :eavt) undo)]
                                   (for [{:keys [e a v added ]} ds :when added]
                                     [[:db/retract e a v]
                                      [:db/add keep a v]]))
                                 (for [ds (map (partial d/datoms db :vaet) undo)]
                                   (for [{:keys [e a v added]} ds :when added]
                                     [[:db/retract  e a v]
                                      [:db/add e a keep]])))
                         flatten
                         (partition 4)
                         (map vec))))))



(defn find-all-by-attr
  "Returns all entities/values containing attribute attr"
  [db attr]
  (d/q '[:find ?e ?v
         :in $ ?a
         :where
         [?e ?a ?v]
         ]
       db
       attr))


(defn recurse-1-entity
  "Grab a single level of recursion through an already-touched entity"
  [db ent]
  (into {} (for [[k v] ent]
             [k (cond
                  (set? v) (set (for [e v]
                                  (if (= (type e) datomic.query.EntityMap)
                                    (->> e :db/id (d/entity db) d/touch)
                                    e)))
                  (= datomic.query.EntityMap (type v)) (->> v :db/id (d/entity db) d/touch)
                  :else  v)])))



(defn read-datomic-edn
  [edn-file]
  (->> edn-file
       slurp
       (clojure.edn/read-string {:readers *data-readers*})))


(comment
  (defn get-unindexed-ea
    "Takes db, an alternate value, entity id, and attribute.
   Searches datoms for that entity with that attribute.
   Returns the value of that datom, or the alternate value (nil, 0, etc) if not present.
   This is an equivalent to an (IF NULL) in SQL."
    [db alt e a]
    (if-let [res (->> (d/q  '[:find  ?v
                              :in $  ?e ?a
                              :where
                              [?e ?a ?v]
                              ]
                            db
                            e
                            a
                            )
                      ffirst)]
      res
      alt))
  )


