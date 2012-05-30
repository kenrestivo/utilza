;;; miscellaneous stuff

(ns utilza.core)



(defn path-to-tree
  [spl path]
  (let [tk (-> path (.split spl) seq rest)]
    [(butlast tk) (last tk)]))

(defn treeify
  "Turn paths and vals into a tree. Via amalloy"
  [paths]
  (reduce
   (fn [m [path value]]
     (assoc-in m `(~@path ::self) value))
   {} paths))

(comment ;;; example

 (treeify (map (partial path-to-tree "/")
                ["/foo/bar/baz/1",
                 "/foo/bar/baz/quux/2"]))
  



  