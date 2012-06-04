(ns utilza.misc
  (:require 
            [clojure.string]))

;;; file system stuff

(defn intermediate-paths
  "Takes file-path, a complete path to the file, including the file iteself!
    Strips off the filename, returns a list of intermediate paths.
    Thanks to emezeke for the reductions device."
  [file-path]
  (let [paths (pop (clojure.string/split file-path  #"\/"))]
    (map #(clojure.string/join "/" %) (rest (reductions conj [] paths)))))



(defn static?
  "Check if a page is static"
  [uri]
  (some #(boolean (re-find % uri)) [#"^/css" #"^/js" #"^/img"]))