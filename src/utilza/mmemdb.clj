(ns utilza.mmemdb
  "A simple file-backed (using nippy) in-memory agent database. 
   Uses the mount library and schema."
  (:require [clojure.core.async :as async]
            [taoensso.nippy :as nippy]
            [clojure.edn :as edn]
            [utilza.java :as ujava]
            [clojure.java.io :as jio]
            [schema.core :as s]
            [mount :as mount]
            [migrator.log :as mlog]
            [taoensso.timbre :as log])
  (:import (java.io File)))


;;; These are the settings to be included in conf file processing
(def Memdb
  {(s/required-key :filename) s/Str
   (s/required-key :autosave-timeout) s/Int})

(defn freeze
  [data filename]
  (-> data
      nippy/freeze
      (jio/copy (java.io.File. filename))))

(defn save-data!
  "Takes settings map and db-internal map"
  [{{:keys [filename]} :settings
    db-agent :db-agent}]
  (let [tmpfile (str filename ".tmp")]
    (send-off db-agent
              (fn [data]
                (log/info "saving db " filename)
                (freeze data tmpfile)
                (.renameTo (File. tmpfile) (File. filename))))))




(defn start-command-loop
  [this]
  (let [cmd-ch (async/chan (async/sliding-buffer 1000))]
    (future (try
              (loop []
                (let [{:keys [cmd data]} (async/<!! cmd-ch)]
                  (case  cmd
                    :save (do (save-data! this)
                              (recur))
                    :quit nil)))
              (catch Exception e
                (log/error e)))
            (log/debug "exiting command loop"))
    (assoc this  :cmd-ch cmd-ch)))



(defn start-autosave-loop
  [this]
  (let [quit-ch (async/chan (async/sliding-buffer 1000))
        {:keys [autosave-timeout]} (:settings this)]
    (future (try
              (loop []
                (let [[{:keys [cmd data]} ch] (async/alts!! [quit-ch (async/timeout autosave-timeout)])]
                  (when (not= quit-ch ch)
                    (log/debug "autosaving database...")
                    (save-data! this)
                    (recur))))
              (catch Exception e
                (log/error e)))
            (log/debug "exiting autosave loop"))
    (assoc this  :autosave-quit-ch quit-ch)))



(defn read-data*
  [path]
  (letfn [(load-db [path]  (some-> path ujava/slurp-bytes nippy/thaw))]
    (try 
      (load-db path)
      (catch Exception e
        (log/warn e)
        (freeze {} path)
        (load-db path)))))



(defn read-data
  [path]
  (let [d (read-data* path)]
    (if (map? d)
      d
      (do (log/warn d "saved data is corrupt? not a map")
          {}))))


(defn load-agent
  [{:keys [settings] :as this}]
  (let [{:keys [filename]} settings]
    (log/info "Loading db first." filename)
    (let [ag (agent (read-data filename)
                    :error-mode :continue
                    :error-handler #(log/error %))]
      (set-validator! ag map?)
      (log/info "DB loaded (presumably)")
      (log/trace @ag)
      ;; TODO: try a test save, just to make sure, and error out if not?
      (assoc this :db-agent ag))))


(s/defn start-memdb
  [settings :- Memdb]
  (log/info "Starting memdb" settings)
  ;;; TODO: make sure i don't load into an agent that already has data?
  (try (-> {:settings settings}
           load-agent
           start-command-loop
           start-autosave-loop)
       (catch Exception e
         (log/error e))))



(defn stop-memdb
  [{:keys [cmd-ch autosave-quit-ch] :as this}]
  (log/info "Shutting down memdb")
  (save-data! this)
  (async/>!! autosave-quit-ch {:cmd :quit})
  (async/>!! cmd-ch {:cmd :quit})
  (merge this {:cmd-ch nil
               :autosave-quit-ch nil
               :db-agent nil}))



(mount/defstate memdb 
  :start  (start-memdb (:db (mount/args)))
  :stop (stop-memdb memdb))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment



  
  )
