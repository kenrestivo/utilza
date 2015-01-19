(ns utilza.file
  (:import java.io.File))


(defn file-names
  "Returns list of file names in directory-name, filtered by a regexp re"
  [directory-name re]
  (for [f (->> directory-name File. .listFiles)
        :when (->> f .getName (re-find re)  boolean)]
    (.getName f)))

(defn assure-cache
  "Nice to have local versions of js files when on flaky network connections.
   Used for example in keeping cached CDN js files around."
  [url cache]
  (try
    (-> cache
        clojure.java.io/input-stream
        .close)
    (catch Exception _
      (->> url
           slurp
           (spit cache)))))


(defn path-sep
  "Basically, basename: Separate a filepath,
   return a vector of [path, basename]"
  [separator s]
  (let [all (.split s separator)]
    [(->> all butlast (interpose "/") (apply str))
     (->  all last)]))