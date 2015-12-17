;;; miscellaneous stuff

(ns utilza.core
  (:require [clojure.set :as set]))



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



(defn un-privatize
  "Takes a namespace and a symbol/var.
  Force getting access to private vars/symbols/functions/etc"
  [ns sym]
  (let [n (if (string? ns) (symbol ns) ns)
        s (if (string? sym) (symbol sym) sym)]
    (deref (ns-resolve (the-ns (symbol n)) (symbol s)))))


(defmacro def-let
  "like let, but binds the expressions globally.
   via http://www.learningclojure.com/2010/09/astonishing-macro-of-narayan-singhal.html"
  [bindings & more]
  (let [let-expr (macroexpand `(let ~bindings))
        names-values (partition 2 (second let-expr))
        defs   (map #(cons 'def %) names-values)]
    (concat (list 'do) defs more)))


(defn map-filter
  "Applies f to coll. Returns a lazy sequence of the items in coll for which
   all the items are truthy. f must be free of side-effects."
  [f coll]
  (filter identity (apply map f coll)))



(defn modify-keys
  "Basically set/rename-keys with the arg order swapped."
  [keymap m]
  (clojure.set/rename-keys map keymap))


(defn mapify
  "Takes a key and a seq of maps.
   Returns a single map with the keys being item k of the original maps,
   and the vals being the rest of the map minus that key"
  [k ms]
  (reduce (fn [acc m]
            (assoc acc (k m) (dissoc m k))) {} ms))


(defn xform-keys
  "Takes a function f and applies it to all keys of map m"
  [f m]
  (zipmap (map f (keys m)) (vals m)))


(defn map-vals
  "Takes a map and applies f to all vals of it"
  [f m]
  (into {} (for [[k v] m] [k (f v)])))


(defn dissoc-vector
  "Given vector v and a seq of positions to dissoc from it,
     returns a vector with those positions removed"
  [v ks]
  (->>  v
        (map-indexed vector)
        (into (sorted-map))
        (#(apply dissoc %  ks))
        vals
        vec))
