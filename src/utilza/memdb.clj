(ns utilza.memdb
  "A very simple atom-backed memory db mostly lifted from
  http://www.brandonbloom.name/blog/2013/06/26/slurp-and-spit/

  Usage: just swap! the db to save data to memory.
  To persist to disk, just call (save-data!)
  To read from disk, just (read-data!)
  You can supply args to read/write from those."
  
  (:import java.io.File)
  (:require [clojure.edn :as edn]
            [environ.core :as env]))



(defonce db (atom []))
(defonce save-agent (agent nil))



(defn save-data!
  "With no args, saves to :db-filename saved in env.
   With one arg, saves to the path/filename specified."
  ([]
     (-> env/env :db-filename save-data!))
  ([dbfilename]
     (let [tmpfile (str dbfilename ".tmp")]
       (send-off save-agent
                 (fn [_]
                   (spit tmpfile (prn-str @db))
                   (.renameTo (File. tmpfile) (File. dbfilename)))))))


(defn read-data!
  "With no args, reads from :db-filename saved in env.
   With one arg, reads from the path/filename specified."
  ([]
     (->> env/env :db-filename read-data!))
  ([dbfilename]
     (reset! db (->> dbfilename slurp edn/read-string))))


