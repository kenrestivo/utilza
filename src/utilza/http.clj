;;; utilities related to http

(ns utilza.http
  (:require [clj-http.client :as client]))




(def cookies (clj-http.cookies/cookie-store))

(defmacro getcook [& args]
  `(binding [clj-http.core/*cookie-store* cookies]
     (client/get ~@args)))


(defmacro putcook [& args]
  `(binding [clj-http.core/*cookie-store* cookies]
     (client/put ~@args)))


(defmacro wrapcook [f & args]
  `(binding [clj-http.core/*cookie-store* cookies]
     (~f ~@args)))



(defmacro spewsave [f & args]
  "Saves the body of the request to a file somewhere, returns the headers"
  `(binding [clj-http.core/*cookie-store* cookies]
     (let [r# (~f ~@args)]
       (spit "/tmp/result.html" (:body r#))
       (dissoc r# :body))))
