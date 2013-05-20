(ns utilza.postgres
  (:require [clojure.java.jdbc :as jdbc]
            [clj-stacktrace.repl :as cst]
            [utilza.log :as log]
            [honeysql.core :as sql]
            [clojure.set :as set]
            [clojure.java.jdbc.sql :as fail]
            [clj-time.coerce :as coerce])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))


;; This is kind of a very cheap wrapper for sql.
;; The mainly useful stuff in her is the spec/pool stuff.
;; The rest could be replaced by something like korma or similar.


(defn spec
  "generate the db spec from the env parameters: db-port, db-name, db-host, db-user, db-pass"
  [envs]
  (let [{:keys [db-host db-port db-name db-user db-pass]} envs]
    {:classname "org.postgresql.Driver" 
     :subprotocol "postgresql"
     :subname (str "//" db-host ":" db-port "/" db-name )
     :user db-user 
     :password db-pass}))



;; ripped from the pages of the jdbc docs
;; added destructuring because it's 2013 now.
(defn pool
  [spec]
  (let [{:keys [subprotocol classname subname user password]} spec]
    {:datasource (doto (ComboPooledDataSource.)
                   (.setDriverClass classname) 
                   (.setJdbcUrl (str "jdbc:" subprotocol ":" subname))
                   (.setUser user)
                   (.setPassword password)
                   ;; expire excess connections after 30 minutes of inactivity:
                   (.setMaxIdleTimeExcessConnections (* 30 60))
                   ;; expire connections after 3 hours of inactivity:
                   (.setMaxIdleTime (* 3 60 60)))}))




;;;;;;;;;;;;;

(defn fix-map
  [keymap]
  (fn [k]
    (if-let [newk (k keymap)]
      newk
      k)))

;; TODO: send the result of fix-keymap to jdbc's column-name transform function, whatever it's called.
(defn modify-keys
  "Transform keys in incoming map m to keys in the db.
   table is name of the table as a keyword"
  [keymap m]
  (zipmap (map (fix-map keymap) (keys m)) (vals m)))

3

(defn insert!
  [table keymap m]
  {:pre [(not (empty? m))]}
  (let [mk (modify-keys keymap m)]
    (try
      (jdbc/insert! (db) table mk)
      (catch Throwable e
        (log/error (str (pr-str m) "\n"
                        (with-out-str
                          (jdbc/print-sql-exception-chain e)
                          (cst/pst e))))
        (throw e)))))



(defn date
  ([] (date (java.util.Date.)))
  ([d] (coerce/to-timestamp d)))


;; TODO: use dire instead.
;; TODO: also (doto (Exception. (str etc)) (.setStackTrace (.getStacktrace e)))
(defn query
  [& args]
  {:pre [(coll? (first args))]} ;; safeguard that should be built into jdbc
  (try
    (apply jdbc/query (cons (db) args))
    (catch Throwable e
      (log/error (str (pr-str args) "\n"
                      (with-out-str
                        (jdbc/print-sql-exception-chain e)
                        (cst/pst e))))
      (throw e))))



(defn empty-as-nil
  [m]
  (if (empty? m) nil m))




(defn where-ize
  "Takes a map of keys/values and turns it into a honeysql and map
   which can be added to the :where key in a map"
  [m]
  (for [[k v] m] [:= k v]))



(defn jdbc-where
  "Petulantly bury the jdbc DSL since I don't like it
   but I can't use honeysql since it doesn't have insert/update yet"
  [m]
  (fail/where m))

(defn update!
  [tablename keymap where-map set-map]
  (let [where-clause (->> where-map (modify-keys keymap) jdbc-where)
        cleaned-set-map (modify-keys keymap set-map)]
    (try
      (jdbc/update! (db) tablename cleaned-set-map where-clause)
      (catch java.sql.BatchUpdateException e
        (log/error (str tablename cleaned-set-map where-clause) e)
        (throw (-> e .getNextException))))))


(defn get-one-id
  [table-name keymap key-id val]
  (->> (sql/build :select :*
                  :from table-name
                  :where [:= key-id val])
       sql/format
       query
       first
       (modify-keys (set/map-invert keymap))
       empty-as-nil))



(defn update-or-insert
  "Updates or inserts a thing"
  [table-name keymap set-map where-map]
  (let [record (modify-keys keymap set-map)
        where-clause (->>  where-map (modify-keys keymap) jdbc-where)]
    (jdbc/db-transaction [t-con (db)]
                         (let [result (jdbc/update! t-con table-name record where-clause)]
                           (if (zero? (first result))
                             (jdbc/insert! t-con table-name record)
                             result)))))



(defn simple-search
  "Takes a table, keymap, and map, and produces a formatted search for matches."
  [table-name keymap m]
  (as-> m <>
        (modify-keys keymap <>)
        (where-ize <>)
        (assoc {} :where <>)
        (merge <> (sql/build :select :* :from table-name))))


(defn valid-pk?
  "Returns true if this record has a vaild multilple primary key"
  [m pks]
  (every? m pks))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  ;; to use this, in your project, do:

  (defonce db-pool (delay (utilza.postgres/pool (utilza.postgres/spec env/env))))

  (defn db
    "This is intended to be the public interface to the database"
    []
    @db-pool)

  ;;; the keys required in your env are:
  [db-host db-port db-name db-user db-pass]

  
  )



