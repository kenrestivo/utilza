;;; misc wrappers around commonly-used java functions

(ns utilza.java
  (:import (java.text SimpleDateFormat)
           java.util.Date
           org.joda.time.DateTime))




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
  [ints]
  (->> ints
       (map unchecked-byte)
       (into-array Byte/TYPE)))


(defn format-bytes
  "Takes a java byte-array, returns a seq of  hex strings with 0x prepended"
  [byte-array]
  (for [b byte-array]
    (->> b
         (bit-and 0xff)
         (format"0x%02x"))))

(defn format-bytes-edn
  "Takes a java byte array, returns EDN string of a seq of hex bytes."
  [byte-array]
  (-> byte-array
      format-bytes
      (as-> x
            (binding [*print-length* 100000 *print-level* 100000]
              (with-out-str (clojure.pprint/pprint x))))
      (.replace "\"" "")))


(defn date-range
  "Generates lazy seq of range of dates incremented by 1 day"
  [start end]
  (take-while #(or (.isBefore %  end) (= % end))
              (iterate #(.plusDays % 1) (DateTime. start))))

(defn write-file
  "Hack to save anarray of bytes to filename.
   Based on http://stackoverflow.com/questions/11321264/saving-an-image-form-clj-http-request-to-file"
  [bytes filename]
  (with-open [w (clojure.java.io/output-stream filename)]
    (.write w bytes)))