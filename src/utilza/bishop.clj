;; utilities for working with bishop

(ns utilza.bishop
  (:require [utilza.repl :as urepl]))



(defn wrap-get-only
  [f]
  (fn [{:keys [request-method params] :as request}]
    (condp = request-method
      :get {:body (f request)})))


(defmacro def-json-get
  "Short-hand for a get-only, json-only resource. Takes the resource name,
    a function for the json body that takes a request as an arg,
    and any other parameters to bishop.core/defresource"
  [res-name f & body]
  `(def ~res-name
     (com.tnrglobal.bishop.core/resource
      {"application/json" (wrap-get-only ~f)}
      ~@body)))



(comment



  )