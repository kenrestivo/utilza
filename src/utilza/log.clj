(ns utilza.log
  (:require [clj-stacktrace.repl :as cst]
            [clojure.tools.logging :as log]))



(defn error
  [data e]
  {:pre [(isa? (class e) Throwable)
         (not (isa? (class data) Throwable))]}
  (log/error (pr-str data) "\n" (with-out-str (cst/pst e))))
