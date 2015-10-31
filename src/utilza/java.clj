(ns utilza.java
  "Misc wrappers around commonly-used java functions. NS is clean for inclusion in Android too."
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



(defn spit-bytes
  "Saves an array of bytes to filename. Has same shape as spit, but not same options.
   Based on http://stackoverflow.com/questions/11321264/saving-an-image-form-clj-http-request-to-file"
  [filename bytes]
  (with-open [w (clojure.java.io/output-stream filename)]
    (.write w bytes)))


(defn invoke-private-method
  "Copy/pasted from https://groups.google.com/forum/#!topic/clojure/6iPNEbe9iZk
   and then refactored because I like pointy things"
  [obj fn-name & args]
  (-> obj
      .getClass
      .getDeclaredMethods
      (as-> x (filter #(->  % .getName (.equals fn-name)) x))
      first
      (.setAccessible true) ;; mutation!
      (as-> m (.invoke obj m args))))


(defn write-file!
  "Convenience function. Takes input stream, and output filename, and does the deed."
  [in out]
  (with-open [w (clojure.java.io/output-stream out)]
    (.write w in)))

(defn get-project-properties
  "Give it the groupid and artifactid of a project and it returns a clojure map of its
   version and git revision id"
  [group-id artifact-id]
  (->> (doto (java.util.Properties.)
         (.load (clojure.java.io/reader
                 (clojure.java.io/resource 
                  (str "META-INF/maven/" group-id "/" artifact-id "/pom.properties")))))
       (into {})
       clojure.walk/keywordize-keys))


(defn seq->enumeration
  "Turns a seq into an Enumeration, i.e. for SequenceInputStream or similar.
   Thanks to clgv"
  [^java.lang.Iterable xs]
  (let [it (.iterator xs)]
    (reify
      java.util.Enumeration
      (hasMoreElements [_]
        (.hasNext it))
      (nextElement [_]
        (.next it)))))

;; via https://stackoverflow.com/questions/23018870/how-to-read-a-whole-binary-file-nippy-into-byte-array-in-clojure
(defn slurp-bytes
  "Slurp the bytes from a slurpable thing."
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy (clojure.java.io/input-stream x) out)
    (.toByteArray out)))
