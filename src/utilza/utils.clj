(ns utilza.utils
  (:require [clojure.zip :as z]))


(defn anchorify
  "Make anchors"
  [s]
  (string/replace s #"[^a-zA-Z]" ""))


(defn tabify
  "Make nice for jquery tabs"
  [m]
  (html [:ul
       (for [x m]
         [:li
          [:a {:href (or (:href x) (->> x  :title anchorify (str "#")))}
           (:title x)]])]
        (for [x m]
          [:div {:id (-> x :title anchorify )} (:body x)])))




(defn map-vals
  "Apply a function to all the values (thanks technomancy)"
  [f m]
  (zipmap (keys m) (map f (vals m))))


;; TODO: make this into a macro, so server/start doesn't have to be pulled in
(defn manual []
  (let [mode :dev
        port 8081]
    (server/start port
                  {:mode mode
                   :ns (-> (.split #"\." (.toString *ns*)) first symbol )})))

(comment
  (defonce srv (manual)))

;;; general couch stuff

(defn save-or-update
  "Silently overwrite a clutch doc if it already present"
  [db m]
  (if-let [found (clutch/get-document db (:_id m))]
    (clutch/update-document db found m)
    (clutch/put-document db m)))


;;; file sysstem stuff

(defn intermediate-paths
  "Takes file-path, a complete path to the file, including the file iteself!
    Strips off the filename, returns a list of intermediate paths.
    Thanks to emezeke for the reductions device."
  [file-path]
  (let [paths (pop (split file-path  #"\/"))]
    (map #(clojure.string/join "/" %) (rest (reductions conj [] paths)))))


(defn hashify [thing]
  (let [md (java.security.MessageDigest/getInstance "SHA-1")]
    (apply str
           (map #(format "%02x" (bit-and 0x00ff %))
                (->> thing  .getBytes  (.digest md ))))))



(defn find-in-zipper
  "Find a node in a zipper, if predicate returns true.
   Assumes that valid nodes are maps, and the structure is a vector-zip."
  [f loc]
  (if (z/end? loc)
    nil
    (if (and (map? (z/node loc))
             (pred loc))
      loc
      (recur pred (z/next loc)))))


(defn get-parents
  "Get all the parents for a particular node.
   Assumes a vector-zip, in the format
    [parent [[child [subchild...]] [anotherchild]]]"
  [n]
  (map (comp z/node)
       (loop [loc (-> n z/up z/up z/left)
              res []]
         (if loc
           (recur (-> loc z/up z/up z/left) (conj res loc))
           res))))


(defn get-children
   "Assumes a vector-zip, in the format
    [parent [[child [subchild...]] [anotherchild]]]"
  [loc]
  (map z/node  (-> loc z/right z/children)))


;;; missing functions from noir

(defn valid-file?
  "Valid file supplied, for noir validation"
  [m]
  (and (> (:size m) 0)
       (:filename m)))


(defn non-zero?
  "Valid number > 0, for noir validation"
  [v]
  (and (not (empty? v))
   (> (Long/parseLong v) 0)))



(defn zip-walk
  "Walks a zipper tree and transforms each node by running f on it."
  [f zcat]
  (loop [loc zcat]
    (if (z/end? loc)
      (z/root loc)
      (recur
       (z/next
        (f loc))))))


(defn cat-tree
  "Turns a zipper into a ul/li tree for hiccup html.
   Format it like so: http://odyniec.net/articles/turning-lists-into-trees/"
  [zcat]
  [:ul.tree
   (zip-walk 
    (fn [loc]
      (cond  (not (z/branch? loc))
             (z/insert-left loc (if (-> loc z/up z/rights) :li :li.last))
             ;; TODO: replace the (and) with the -?> macro from incubator
             (and (-> loc z/down z/down) (-> loc z/down z/down z/branch? not))
             (z/insert-left (z/down loc) :ul)
             :else loc))
    zcat)])