;;; repl-related utilities

(ns utilza.repl
  (:require [clojure.reflect]
            [clojure.pprint :as pprint]
            [clojure.string])
  (:use [clojure.pprint]
        [org.timmc.handy.repl :only [show] :rename {show hjpub}]))



(defn jpublics 
  "getting only non-private members of a java class.
  PLEASE don't make me read javadooc!"
  [c]
  ;; optimization by amalloy
  (->> c
       clojure.reflect/reflect 
       :members 
       (remove (comp :private :flags))
       (map :name)
       sort
       clojure.pprint/pprint))


(defn hjall [o]
  (org.timmc.handy.repl/show o {:inherit :true} ))

(defn jmethods
  "get the details on  methods/members. "
  ([obj] (jmethods obj ""))
  ([obj srch]
   (filter #(.contains (str (:name %)) srch)
           (:members (clojure.reflect/reflect obj)))))


(defn methods-pub 
  "get names and arities/args on public members/methods"
  ([obj] (methods-pub obj ""))
  ([obj srch]
   (map (juxt :name :parameter-types)
        (filter #(and
                  (.contains (str (:name %)) srch)      
                  ((comp :public :flags) %))
                (:members (clojure.reflect/reflect obj))))))



(defn publics 
  "format public vars in a ns nicely"
  [ns]
  (-> ns  ns-publics keys sort clojure.pprint/pprint))


(defn cp []
  (clojure.string/split (System/getProperty "java.class.path") #":"))


(defn inv [x]
  (/ 1  x))

(defn in-libs 
  "finds some string in the mire of the loaded libs"
  [re]
  (filter #(.contains (str %) re) (loaded-libs)))


(defn all-libs
  "show all the libs, just for grunts"
  []
  (doseq [l (-> (loaded-libs) sort)]
    (println l)))


(defn in-cp 
  "finds some string in the mire of the loaded classpath"
  [re]
  (filter #(.contains (str %) re) (cp)))



(defn env
  "produce the system env in a readable, more clojureish-looking  format"
  []
  (into {} (System/getenv)))



(defn props
  "produce the system env in a readable, more clojureish-looking  format"
  []
  (into {} (System/getProperties)))


(comment
  ;; thisja is probably naughty, but i'd need lien2 to get profiles
  (ns user)
  (alter-var-root *print-length* (constantly 103))
  (alter-var-root *print-level* (constantly 15)))





;; i use this all the time
(defn reloadns
  ([]
   (reloadns (symbol (.getName *ns*)) :reload))
  ([tns & all]
   (let [flag (if all :reload-all :reload)]
     (require tns flag))))

(defn reload-enter
  "load the ns and then get into it"
  [tns & all]
  (reloadns tns all)
  (in-ns tns))


(defn spew
  "pretty-print dumps a potentially-massive clojure object into a buffer
   that will have proper clojure syntax highlighing.
   This is because pprint in nrepl doesn't syntax highlight, it's all blue.
   And, this keeps a running log too."
  [description  structure]
  (spit "/tmp/log.clj" (str "\n\n;; " description "\n") :append true)
  (spit "/tmp/log.clj" (with-out-str (clojure.pprint/pprint structure)) :append true))



(defn massive-spew
  "Takes a filename to spew to, and a clojure map/tree.
  Writes a deeply pretty-printed tree to the filename"
  [out-filename m]
  (binding [*print-length* 10000 *print-level* 10000]
    (->> m
         (#(with-out-str (clojure.pprint/pprint %)))
         (spit out-filename))))


(defmacro def-let
  "Like let, but binds the expressions globally.
   Lifted from http://www.learningclojure.com/2010/09/astonishing-macro-of-narayan-singhal.html"
  [bindings & more]
  (let [let-expr (macroexpand `(let ~bindings))
        names-values (partition 2 (second let-expr))
        defs   (map #(cons 'def %) names-values)]
    (concat (list 'do) defs more)))


(defn print-table-out
  "print-table but returns a string value"
  [ms]
  (binding [*print-length* 10000 *print-level* 10000]
    (with-out-str (pprint/print-table ms))))
