(ns utilza.enlive
  (:require clojure.walk
            clojure.pprint))

;; utilities for dealing with enlive tree structures


(defn un-html*
  "Removes the HTML from the enlive, leaving only the tree structure"
  [x]
  (if-not (map? x)
    x
    (update-in x [:content]  #(if-let [m (filter map? %)]
                                m
                                %))))


(defn un-html
  "Removes the HTML from the enlive, leaving only the tree structure"
  [tag]
  (clojure.walk/postwalk un-html* tag))



(defn spew-un-html
  "Takes a filename to spew to, and an Enlive tag set snippet.
  Writes a deeply pretty-printed, cleaned tree to the filename"
  [out-filename tag]
  (binding [*print-length* 10000 *print-level* 10000]
    (->> tag
         un-html
         (#(with-out-str (clojure.pprint/pprint %)))
         (spit out-filename))))