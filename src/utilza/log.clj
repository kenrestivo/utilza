(ns utilza.log
  (:require [clj-stacktrace.repl :as cst]
            [taoensso.timbre :as log]))



(defmacro catcher 
  "Executes body within a try/catch, and logs the error using TImbre"
  [body]
  `(try
     ~body
     (catch Exception e#
       (log/error e#))))
