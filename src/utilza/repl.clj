;;; repl-related utilities

(ns utilza.repl
  (:require [clojure.reflect]
            [clojure.pprint]
            [clojure.string])
  (:use [clojure.pprint]
        [org.timmc.handy.repl :only [show] :rename {show hjpub}]))



(defn jpublics 
  "getting only non-private members of a java class.
  PLEASE don't make me read javadooc!"
  [c]
  ;; optimization by amalloy
  (->
   (map :name
        (remove (comp :private :flags)
                (:members (clojure.reflect/reflect c))))
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
  (-> (loaded-libs) seq sort pprint))


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


(defn spew [h b]
  (spit "/tmp/log.clj" (str "\n\n;;" h "\n") :append true)
  (spit "/tmp/log.clj" (with-out-str (clojure.pprint/pprint b)) :append true))
