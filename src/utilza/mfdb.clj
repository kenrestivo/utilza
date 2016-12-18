(ns utilza.mfdb
  "A simple file-backed (using edn) in-memory agent database. 
   Uses the mount library."
  (:require [clojure.core.async :as async]
            [clojure.edn :as edn]
            [utilza.java :as ujava]
            [clojure.java.io :as jio]
            [mount.core :as mount]
            [taoensso.timbre :as log])
  (:import (java.io File)))


;; TODO: perform this-ectomy
;; TODO: spec!
;; TODO: unit tests

(defn save-data!
  "Takes settings map and db-internal map"
  [{{:keys [filename]} :settings
    db-agent :db-agent}]
  (let [tmpfile (str filename ".tmp")]
    (send-off db-agent
              (fn [data]
                (let [tmpfile (str filename ".tmp")]
                  (log/info "saving db " filename)
                  (some->> data
                           prn-str
                           (spit tmpfile))
                  (.renameTo (File. tmpfile) (File. filename)))))))



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
  "Separate channel/loop because timeout"
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
                (log/error e "autosave loop failed")))
            (log/debug "exiting autosave loop"))
    (assoc this  :autosave-quit-ch quit-ch)))


(defn load-db
  [path]
  (some-> path 
          slurp 
          edn/read-string))

;; TODO: robert bruce or supervisor or similar?
(defn read-data*
  [path]
  (or (try (load-db path)
           (catch Throwable e
             (log/warn e path "didn't load on first try")))
      ;; let higher layers catch and report or fail
      (spit path "{}") ;; create if not exist
      (load-db path)))




(defn read-data
  [path]
  (let [d (read-data* path)]
    (if (map? d)
      d
      (do (log/warn d "saved data is corrupt? not a map")
          {}))))

                                        ;
(defn load-agent
  [{:keys [settings] :as this}]
  (let [{:keys [filename]} settings]
    (log/info "Loading db first." filename)
    (let [ag (agent (read-data filename)
                    :error-mode :continue
                    :error-handler #(log/error %))]
      (set-validator! ag map?)
      (assert (boolean @ag))
      (log/info "DB loaded (presumably)")
      (log/trace @ag)
      ;; TODO: try a test save, just to make sure, and error out if not?
      (assoc this :db-agent ag))))



(defn start-memdb
  [settings]
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
  (doseq [c [autosave-quit-ch cmd-ch]]
    (async/>!! c {:cmd :quit}))
  (merge this {:cmd-ch nil
               :autosave-quit-ch nil
               :db-agent nil}))



(mount/defstate memdb 
  :start  (-> (mount/args) :db start-memdb)
  :stop (stop-memdb memdb))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  (-> {:db {:filename "/tmp/foo.db"
            :autosave-timeout 50000}}
      mount/with-args
      mount/start)

  (log/set-level! :trace)

  (mount/stop)  
  
  )


