(ns utilza.tcp
  (:require [clojure.core.async :as async])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

;; adapted from http://nakkaya.com/2010/02/10/a-simple-clojure-irc-client/



(defn connect
  [address port bufsiz]
  (let [socket (Socket. address port)
        in-stream (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out-stream (PrintWriter. (.getOutputStream socket))
        out-chan (async/chan bufsiz)
        in-chan (async/chan bufsiz)]
    {:in-stream in-stream
     :out-stream out-stream
     :out-chan out-chan
     :in-chan in-chan
     :in-thread  (future  (while (not (nil? in-stream))
                            (let [msg (.readLine in-stream)]
                              (async/>!! in-chan msg))))
     :out-thread (future  (while (not (nil? out-stream))
                            (let [msg (async/<!! out-chan)]
                              (doto out-stream
                                (.println msg)
                                (.flush)))))}))



(defn disconnect
  [{:keys [in-stream out-stream in-chan out-chan socket]}]
  (.close in-stream)
  (.close out-stream)
  (async/close! out-chan)
  (async/close! in-chan)
  (.close socket)
  nil)



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(comment

  (def foo (connect "localhost" 1632 10))

  (async/>!! (:out-chan foo) "fooobar")
  
  (async/<!! (:in-chan foo))


  (disconnect foo)
  
  )