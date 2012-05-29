;;; utilities related to http

(ns utilza.http
  (:require [clj-http.client :as client]))




(def cookies (clj-http.cookies/cookie-store))

(defmacro getc [& args]
  `(binding [clj-http.core/*cookie-store* cookies]
     (client/get ~@args)))


(defmacro putc [& args]
  `(binding [clj-http.core/*cookie-store* cookies]
     (client/put ~@args)))


(defmacro wrapc [f & args]
  `(binding [clj-http.core/*cookie-store* cookies]
     (~f ~@args)))


 
(defmacro spewsave [& body]
  "Give it the function and args, i.e. client/get url, but without parens.
   Saves the :body of the request to /tmp/result.html, and returns the headers."
  `(binding [clj-http.core/*cookie-store* cookies]
     (let [r# (try (~@body) (catch Exception e# (-> e# .getData :object)))]
       (spit "/tmp/result.html" (:body r#))
       (dissoc r# :body))))



(defmacro just-gimme
  "Oh, FFS!"
  []
  (require '[clj-http.client :as client]))