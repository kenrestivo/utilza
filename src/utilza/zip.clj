(ns utilza.zip
  (:require [clojure.zip :as z])
  (:use [clojure.core.incubator :only [-?>]]))


(defn map-vals
  "Apply a function to all the values (thanks technomancy)"
  [f m]
  (zipmap (keys m) (map f (vals m))))





(defn find-in-zipper
  "Find a node in a zipper, if predicate returns true.
   Assumes that valid nodes are maps, and the structure is a vector-zip."
  [pred? loc]
  (if (z/end? loc)
    nil
    (if (and (map? (z/node loc))
             (pred? loc))
      loc
      (recur pred? (z/next loc)))))


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
             (-?> loc z/down z/down z/branch? not)
             (z/insert-left (z/down loc) :ul)
             :else loc))
    zcat)])

