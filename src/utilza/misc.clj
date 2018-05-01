(ns utilza.misc
  (:require 
   [clojure.string :as string]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.walk :as walk]))

;;; file system stuff

(defn intermediate-paths
  "Takes file-path, a complete path to the file, including the file itself!
    Strips off the filename, returns a list of intermediate paths.
    Thanks to emezeke for the reductions device."
  [file-path]
  (let [paths (pop (string/split file-path  #"\/"))]
    (map #(string/join "/" %) (rest (reductions conj [] paths)))))



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


(defn columnify
  "Converts a vector of vectors into a vector of maps,
   which makes it into a  table for print-table,
   adding the headers as column headings"
  ([headers rows]
   (map (partial zipmap headers) rows))
  ([[headers & rows]]
   (columnify headers rows)))



(defn uncolumnify
  "Takes a sequence of maps.
   Returns the keys as the first row, followed by all the rows of values."
  [ms]
  (apply cons ((juxt (comp keys first) (partial map vals)) ms)))

(defn uncolumnify-headers
  "Takes a sequence of maps.
   Returns the keys as the first row, followed by all the rows of values."
  [ms]
  ((juxt (comp keys first) (partial map vals)) ms))


(defn munge-columns
  "Takes a map km of keys and functions, and a map m,
   and updates the map to change all values applying
   function to the key in map m"
  [km m]
  (reduce (fn [m [k f]]
            (if (contains? m k) (update-in m [k] f) m))
          m km))

(defn munge-all-columns
  "Takes a coll of maps and a key map. Returns them with all the values coerced via key-map"
  [key-map ms]
  (for [m ms]
    (munge-columns key-map m)))

(defn capitalize-words
  "Capitalize the first letter of every word in string s
  For cases where you don't want to import
  http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/text/WordUtils.html"
  [s]
  (->> (for [s (clojure.string/split s #"\W+")]
         (clojure.string/capitalize s))
       (interpose " ")
       (apply str)))

(defn loop-delay
  "Excecutes no-args function f endlessly, with delay millisecond delay in between.
   Returns the Thread so you can cancel it, etc."
  [f delay]
  (Thread.  #(while true
               (do
                 (f)
                 (Thread/sleep delay)))))


(defn display-bucket
  [divisor [k v]]
  [(-> k  (* divisor) (+ divisor) (- divisor) int) v])

(defn bucket-frequencies
  [divisor vals]
  (->> (for [v vals]
         (-> v
             (/  divisor)
             int))
       frequencies
       (map (partial display-bucket divisor))))



(defn sort-map-fn
  "Takes a vector of keys in the order you want them sorted,
   returns a function that will take a map and return a
   sorted map with the keys in the right order."
  [ordered-keys]
  #(into (sorted-map-by
          (fn [k1 k2]
            (< (.indexOf ordered-keys k1) (.indexOf ordered-keys k2)))) %))


;; TODO: take a sort-by function as an arg
(defn make-histogram-cumulative
  "Takes a histogram of {v c, ... } where c is the count of occurences and v is the value.
  Returns a histogram of [[v cumulative-c] .. ] in descending order of v."
  [histogram]
  (let [sorted (->> histogram (sort-by first) reverse)
        vs (map second sorted)
        ks (map first sorted)]
    (map vector ks (reduce (fn [acc v]
                             (conj acc (+' v (or (last acc) 0))))
                           []
                           vs))))

(defn inter-str
  "Takes seq and optional separator (space is default)
   and returns a string with the seq interposed with the separator"
  ([separator xs]
   (apply str (interpose separator  xs)))
  ([xs]
   (inter-str " " xs)))

;; TODO: Maybe replace with walk?
(defn foobar
  "I don't even know how to describe this, except that you'd use it like:
  (swap! some-atom foobar map assoc-in  [:x] y)"
  [xs coll-f inner-f & args]
  (into (empty xs)
        (coll-f #(apply inner-f % args) xs)))

(defn unique-values
  "Takes a map. Returns true if the values are unique
   (i.e. safe to be used as keys by clojure.set/invert-map)"
  [m]
  (let [vs (vals m)]
    (= (distinct vs) vs)))

(defn basename
  "Strip the . suffix from a string s.
   Doesn't matter what the suffix is (unilke unix basename)."
  [s]
  (if s
    (string/join (butlast (string/split s  #"\.")))
    ""))

(defn escape-html
  [text]
  (clojure.string/escape text {"&"  "&amp;"
                               "<"  "&lt;"
                               ">"  "&gt;"
                               "\"" "&quot;"}))


(defn redact
  "Takes a map and a key.
   Walks the map, if the key is anywhere in there, redacts it.
   Used for hiding passwords in log files."
  [m k]
  (walk/postwalk  #(if (and (map? %) (k %))
                     (assoc % k "[REDACTED]")
                     %)
                  m))


(defn redact-keys
  "Takes a map and a coll of keys.
   Walks the map, if the key is anywhere in there, redacts it.
   Used for hiding passwords in log files."
  [m ks]
  (reduce redact m ks))


(defn changed-keys
  "Returns a set of keys in map bm that are not present in map am"
  [am bm]
  (apply disj (-> bm keys set) (keys am)))


(defn split-seq 
  "Generates a list of from-to steps between 0 and max, by step"
  [step max] 
  (partition 2 1 (concat (range 0 max step) [max])))


(defn dissocs
  "dissoc k from coll of maps ms"
  [k ms]
  (map #(dissoc % k) ms))


(defn groupify
  "Takes k and coll of maps ms. 
   Groups the maps by k into a map of maps, and dissocs k from the maps."
  [k ms]
  (into {}
        (for [[kv vs] (group-by k ms)]
          [kv (dissocs k vs)])))




(defn flatten-map
  "converts a nested map into a flattened map
   via via http://stackoverflow.com/questions/17901933/flattening-a-map-by-join-the-keys"
  ([form]
   (into {} (flatten-map form nil)))
  ([form pre]
   (mapcat (fn [[k v]]
             (let [prefix (if pre (conj pre k) [k])]
               (if (map? v)
                 (flatten-map v prefix)
                 [[prefix v]])))
           form)))

(defn seqs->org-table
  "Takes a seq of seqs, outputs a string in org mode or jira table"
  [seqs]
  (->> seqs
       (map (partial inter-str "|"))
       (inter-str "|\n|")
       (format "|%s|")))


(defn read-lines
  "Greedily reads a file into a seq of lines"
  [fname]
  (with-open [rdr (clojure.java.io/reader fname)]
    (doall (line-seq rdr))))



(defn val-freqs
  "Takes a seq of maps. 
  Returns a map with the frequencies of all values for all keys"
  [ms]
  (into {}
        (for [k  (->> ms
                      (map keys)
                      (apply concat)
                      set)]
          [k (->> ms
                  (map k)
                  frequencies)])))


(defn update-with-function
  "Applies f to values of acc and map m
      for all values with keys"
  [keys f acc m]
  (reduce (fn [acc1 k]
            (update-in acc1 [k] f (k m)))
          acc
          keys))




