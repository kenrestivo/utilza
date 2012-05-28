(ns utilza.noirutils.misc
  (:require [noir.core]
            [noir.util.test]
            [noir.server :as server]
            [noir.core]
            [noir.server.handler]
            [noir.request]
            [noir.util.test])
  (:use [clojure.pprint :only [pprint]]))




;;; missing functions from noir 1.2x, present in 1.3.0-beta7  though

(defn valid-file?
  "Valid file supplied, for noir validation"
  [m]
  (and (> (:size m) 0)
       (:filename m)))


(defn non-zero?
  "Valid number > 0, for noir validation"
  [v]
  (and (not (empty? v))
       (> (Long/parseLong v) 0)))



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






(defn mock
  "A very simple from-the-repl way to test/debug webpages"
  [ring]
  (let [res (noir.util.test/with-noir (noir.util.test/send-request-map ring))]
    (spit "/tmp/log.html" (:body res))
    (pprint ((juxt :status :headers) res))))
 


(defn spew-req
  "str with pprint of the ring request.
  Wrepped in pre so it is  readable both in browser
   and in shell with curl, etc. Useful for debugging."
  []
  [:pre (-> (noir.request/ring-request)
            clojure.pprint/pprint
            with-out-str)])



;; the OLD way
(defn manual []
  (let [mode :dev
        port 8081]
    (server/start port
                  {:mode mode
                   :ns (-> (.split #"\." (.toString *ns*)) first symbol )})))

(comment
  (defonce srv (manual)))



;;; the NEW way


(defn reloader
  "Pass it your noir-project-name.server/-main function.
   Save the result, and call that when you need to regenerate all handlers."
  ([mn]
     (reloader mn nil))
  ([mn srv]
     (when srv (.stop srv))
     (let [srv (mn)]
       (fn []
         (reloader mn srv)))))
       


(comment ;; example
;;; NOTE VAR QUOTE! CRUCIAL!
  (def rel (reloader #'-main))
  (def rel (rel))
  ;; do some work, need to reload middleware
  (def rel (rel))
  ;; do soem more work, damn, need to update middleware again
  (def rel (rel)))
;; ... etc)

