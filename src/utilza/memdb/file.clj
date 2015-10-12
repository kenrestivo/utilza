(ns utilza.memdb.file
  "A very simple atom-backed memory db mostly lifted from
  http://www.brandonbloom.name/blog/2013/06/26/slurp-and-spit/

  Usage: just swap! the db to save data to memory.
  To persist to disk, just call (save-data!)
  To read from disk, just (read-data!)
  You can supply args to read/write from those."
  
  (:import java.io.File)
  (:require [clojure.edn :as edn]
            [taoensso.timbre :as log]
            [environ.core :as env]))



(defonce db (atom []))
(defonce save-agent (agent nil))



(defn save-data!
  "With no args, saves to :db-filename saved in env.
   With one arg, saves to the path/filename specified."
  ([]
     (-> env/env :db-filename save-data!))
  ([dbfilename]
     (binding [*print-length* 10000000 *print-level* 10000000]
       (let [tmpfile (str dbfilename ".tmp")]
         (send-off save-agent
                   (fn [_]
                     (log/info "saving db " dbfilename)
                     (spit tmpfile (prn-str @db))
                     (.renameTo (File. tmpfile) (File. dbfilename))))))))


(defn read-data!
  "With no args, reads from :db-filename saved in env.
   With one arg, reads from the path/filename specified."
  ([]
     (->> env/env :db-filename read-data!))
  ([dbfilename]
     (reset! db (->> dbfilename slurp edn/read-string))))




(defn load []
  (if (< 0 (count @db))
    (log/warn "Cowardly refusing to load db, it looks like it's already loaded")
    (do (log/info "Loading db first." (:db-filename env/env))
        (read-data!)
        (log/info "DB loaded (presumably)"))))
