(ns utilza.noirutils.misc
  (:require [noir.core]
            [noir.util.test]
            [noir.server :as server]
            [noir.core]
            [noir.server.handler]
            [noir.request]
            [noir.util.test])
  (:use [clojure.pprint :only [pprint]]))






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

