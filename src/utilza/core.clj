;;; miscellaneous stuff

(ns utilza.core)



(defn path-to-tree
  "Split a path into a vector of parents and the node."
  [spl path]
  (let [tk (-> path (.split spl) seq rest)]
    [(butlast tk) (last tk)]))

(defn treeify
  "Turn [[paths val]] into a tree. Via amalloy.
   Can only have one self per node."
  [paths]
  (reduce
   (fn [m [path value]]
     ;; This quoting business is here only to make ::self qualified in the utilza.core ns
     ;; Could make it :utilza.core/self and blow off the quoting, if that's less weird.
     (assoc-in m `(~@path ::self) value))
   {} paths))


(defn select-and-rename
  "Takes a target map, and a map of {:old-key :new-key}
   Rename the keys in the target map, selecting only those which match.
   Used for pulling crap out of env, etc."
  [map kmap]
  (clojure.set/rename-keys (select-keys map (keys kmap))
                           kmap))

(comment ;;; example
  (treeify (map (partial path-to-tree "/")
                ["/foo/bar/baz/1",
                 "/foo/bar/baz/quux/3"])))
  



  