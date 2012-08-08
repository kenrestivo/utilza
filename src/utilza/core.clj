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
     (assoc-in m `(~@path ::self) value))
   {} paths))


(defn select-and-rename
  "Rename keys, selecting only those which match."
  [map kmap]
  (clojure.set/rename-keys (select-keys map (keys kmap))
                           kmap))

(comment ;;; example
  (treeify (map (partial path-to-tree "/")
                ["/foo/bar/baz/1",
                 "/foo/bar/baz/quux/3"])))
  



  