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
  (some #(re-find % uri) [#"^/css" #"^/js" #"^/img"]))


(defn reconstruct-url
  "reconstructs the original url from the ring map"
  [req & strip-query?]
  (str
   (-> req :scheme name)
   "://"
   (:server-name req)
   (when-not (#{80 443} (:server-port req))
     (str ":" (:server-port req)))
   (:uri req)
   (when-not (or (empty? (:query-string req)) strip-query?)
     (str "?" (:query-string req)))
   ))



(defn to-hex-array
  "Generate static byte arrays for C programming"
  [s]
  (-> (for [i (seq s)]
        (->> i
             int
             Integer/toHexString
             (str "0x")))
      ((partial interpose ","))
      pr-str
      (clojure.string/replace #"[\"\(\)]" "")))