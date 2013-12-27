;;; misc wrappers around commonly-used java functions

(ns utilza.java
  (:import (java.text SimpleDateFormat)
           java.util.Date))




(defn hashify [thing]
  (let [md (java.security.MessageDigest/getInstance "SHA-1")]
    (apply str
           (map #(format "%02x" (bit-and 0x00ff %))
                (->> thing  .getBytes  (.digest md ))))))


(defn iso8601-to-rfc822-date
  "For converting java format dates to javscript format dates
   XXX NOTE this should be unnecesary now that we have #inst reader literal in 1.4"
  [isodate]
  (-> (SimpleDateFormat. "EEE, d MMM yyyy HH:mm:ss Z")
      (.format
       (-> (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ")
           (.parse isodate
                   (java.text.ParsePosition. 0))))))


(defn pseudo-uuid
  "Not as cryptographically secure as hashify or UUID,
   but close enough for rock n roll, and easier to read/copy/paste."
  []
  (Long/toString (rand Long/MAX_VALUE) 36))

(defn java-thrush
  "Takes an object, method, and args.
  For using -> with java methods that return void."
  [o m & args]
  (apply m o args))


(defn epoch->java-date
  "Convert a unix epoch to a java date (clojure #inst)"
  [n]
  (-> n (* 1000) Date. ))

(defn byteify
  "Turns seq of integers or bytes into java byte array"
  [s]
  (->> s
       (map unchecked-byte)
       (into-array Byte/TYPE)))