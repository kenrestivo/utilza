(ns utilza.unix
  (:require  [me.raynes.conch.low-level :as sh]
             [clojure.java.io :as jio]))


(defn get-ip-address
  []
  (try
    (->   (->>  (sh/proc "/bin/ip" "addr" "show" "eth0")
                :out
                jio/reader
                line-seq
                (filter #(.contains % "inet"))
                (remove #(.contains % "inet6"))
                first)
          (clojure.string/split #" ")
          (nth 5)
          (clojure.string/split #"/")
          first)
    (catch Exception e
      (log/error "can't get ip address")
      "Network error"))))