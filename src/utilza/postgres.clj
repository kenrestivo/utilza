(ns utilza.postgres
  (:require [clojure.java.jdbc :as jdbc]
            [clj-stacktrace.repl :as cst]
            [taoensso.timbre :as log]
            [utilza.core :as cora]
            [clj-time.coerce :as coerce]
            [honeysql.core :as sql]
            [clojure.set :as set]))



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




;;;;;;;;;;;;;


(defn insert!
  [db table keymap m]
  {:pre [(not (empty? m))]}
  (let [mk (utilza.core/modify-keys keymap m)]
    (try
      (jdbc/insert! db table mk)
      (catch Throwable e
        (log/error e (str (pr-str m) "\n"
                          (with-out-str
                            (jdbc/print-sql-exception-chain e))))
        (throw e)))))



(defn date
  ([] (date (java.util.Date.)))
  ([d] (coerce/to-timestamp d)))


;; TODO: use dire instead.
;; TODO: also (doto (Exception. (str etc)) (.setStackTrace (.getStacktrace e)))
(defn query
  [db & args]
  {:pre [(coll? (first args))]} ;; safeguard that should be built into jdbc
  (try
    (apply jdbc/query (cons db args))
    (catch Throwable e
      (log/error e (with-out-str (jdbc/print-sql-exception-chain e)))
      (throw e))))



(defn empty-as-nil
  [m]
  (if (empty? m) nil m))




(defn where-ize
  "Takes a map of keys/values and turns it into a honeysql and map
   which can be added to the :where key in a map"
  [m]
  (for [[k v] m] [:= k v]))




(defn get-one-id
  [table-name keymap key-id val]
  (->> (sql/build :select :*
                  :from table-name
                  :where [:= key-id val])
       sql/format
       query
       first
       (utilza.core/modify-keys (set/map-invert keymap))
       empty-as-nil))



(defn simple-search
  "Takes a table, keymap, and map, and produces a formatted search for matches."
  [table-name keymap m]
  (as-> m <>
        (utilza.core/modify-keys keymap <>)
        (where-ize <>)
        (assoc {} :where <>)
        (merge <> (sql/build :select :* :from table-name))))


(defn valid-pk?
  "Returns true if this record has a vaild multilple primary key"
  [m pks]
  (every? m pks))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment


  
  )



