(ns utilza.enlive
  (:require clojure.walk))

;; utilities for dealing with enlive tree structures


(defn un-html*
  "Removes the HTML from the enlive, leaving only the tree structure"
  [x]
  (if-not (map? x)
    x
    (update-in x [:content]  (partial filter map?))))


(defn un-html
  "Removes the HTML from the enlive, leaving only the tree structure"
  [tag]
  (clojure.walk/postwalk un-html* tag))