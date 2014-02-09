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
   Wraps this in a local cookie store.
  If there are exceptions, returns them instead of throwing them.
   Saves the :body of the request to /tmp/result.html, and returns the headers."
  `(binding [clj-http.core/*cookie-store* cookies]
     (let [r# (try (~@body) (catch Exception e# (-> e# .getData :object)))]
       (spit "/tmp/result.html" (:body r#))
       (dissoc r# :body))))


(defmacro spew-nocookie [& body]
  "Give it the function and args, i.e. client/get url, with parens.
  Doesnt' do anything special with cookie stores.
  If there are exceptions, returns them instead of throwing them.
   Saves the :body of the request to /tmp/result.html, and returns the headers and status."
  `(let [r# (try ~@body (catch Exception e# (-> e# .getData :object)))]
     (spit "/tmp/result.html" (:body r#))
     (dissoc r# :body)))

(defmacro just-gimme
  "Oh, FFS!"
  []
  (require '[clj-http.client :as client]))




(comment
  ;; does not work but i'd like it to
  (defmacro only-200
    [& opts]
    (let [s (:status `(spewsave ~@opts))]
      (when (< s 400) (slurp "/tmp/result.html")))))


