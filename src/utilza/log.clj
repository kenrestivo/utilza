(ns utilza.log
  (:require 
   [utilza.repl :as urepl]
   [taoensso.timbre :as log]))



(defmacro catcher 
  "Executes body within a try/catch, and logs the error using TImbre"
  [& body]
  `(try
     ~@body
     (catch Exception e#
       (log/error e#)
       :error)))


(defn try-times-p*
  [max time f p?]
  (loop [n 1]
    (if (< max n)
      (try
        (f)
        (catch Throwable t
          (log/error t)))
      (do 
        (try
          (f)
          (catch Throwable t
            (if (p? t)
              (do (log/warn (.getMessage t))
                  (Thread/sleep (* time n)))
              (log/error t))))
        (recur (inc n))))))


;; ugly, but does the job
(defn try-times*
  [max time f]
  (loop [n 1]
    (if (< max n)
      (try
        (f)
        (catch Throwable t
          (log/error t)))
      (do 
        (try
          (f)
          (catch Throwable t
            (log/warn (.getMessage t))
            (Thread/sleep (* time n))))
        (recur (inc n))))))


(defmacro try-times
  "Retries function with backoff. 
   Logs warnings on retry, error after retries expired"
  [times retry-wait & body]
  `(try-times* 
    ~times 
    ~retry-wait
    (fn [] ~@body)))

(defmacro spewer
  "Executes body within a try/catch, spews the result to /tmp/foo.edn, and logs any errors to timbre"
  [& body]
  `(try
     (spit "/tmp/foo.edn" "")
     (urepl/massive-spew
      "/tmp/foo.edn"
      ~@body)
     (catch Exception e#
       (log/error e#)
       :error)))

