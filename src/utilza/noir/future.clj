;;; stuff that will be in noir but hasn't been released/approved yet

(ns utilza.noirutils.future
  (:require [noir.core]
            [noir.util.test]
            [noir.server :as server]
            [noir.core]
            [noir.server.handler]
            [noir.request]
            [noir.util.test])
  (:use [clojure.pprint :only [pprint]]))



(defn valid-file?
  "Valid file supplied, for noir validation"
  [m]
  (and (> (:size m) 0)
       (:filename m)))



(defn valid-number?
  "Returns true if the string can be cast to a Long"
  [v]
  (try
    (Long/parseLong v)
    true
    (catch Exception e
      false)))


(defn greater-than?
  "Returns true if the string represents a number > given."
  [v n]
  (and (valid-number? v)
       (> (Long/parseLong v) n)))


(defn less-than?
  "Returns true if the string represents a number < given."
  [v n]
  (and (valid-number? v)
       (> (Long/parseLong v) n)))



;;; submitted as patch for noir 1.3.0-beta8, but not in yet.
(defn noir-state
  "Print the state of the noir server's routes/middleware/wrappers.
   If optional details? arg is truthy, show noir-routes, route-funcs,
   and status pages too."
  [& details?]
  (let [print-func (if details? pprint (comp println pprint sort keys))]
    
    (println "== Pre-Routes ==")
    (print-func @noir.core/pre-routes)
  
    (println "== Routes and Funcs ==")
    (print-func (merge-with vector @noir.core/noir-routes  @noir.core/route-funcs))
  
    (println "== Post-Routes ==")
    (pprint @noir.core/post-routes)
  
    (println "== Compojure-Routes ==")
    (pprint @noir.core/compojure-routes)
  
    (println "== Middleware ==")
    (pprint @noir.server.handler/middleware)
  
    (println "== Wrappers ==")
    (pprint @noir.server.handler/wrappers)

    (println "== Memory Store ==")
    (pprint @noir.session/mem)
  
    (when details?
      (do (println "== Status Pages ==")
          (pprint @noir.statuses/status-pages)))))



