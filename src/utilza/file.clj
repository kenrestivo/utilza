(ns utilza.file
  (:import java.io.File))


(defn file-names
  "Returns list of file names in directory-name, filtered by a regexp re"
  [directory-name re]
  (for [f (->> directory-name File. .listFiles)
        :when (->> f .getName (re-find re)  boolean)]
    (.getName f)))