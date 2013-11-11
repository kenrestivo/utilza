;; utilities for working with bishop

(ns utilza.bishop
  (:require [utilza.repl :as urepl]))


(defmacro json-get
  "Short-hand for a get-only, json-only resource. Takes as args a vector of the names
   of the vars  used to represent the request and params, and a  body to execute."
  [[req-binding param-binding] body]
  `{"application/json" (fn [{:keys [request-method# ~param-binding] :as ~req-binding}]
                         (condp = request-method#
                           :get {:body ~body}))})



(comment



  )