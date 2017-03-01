(ns utilza.coercions
  "Via https://gist.github.com/ikitommi/17602f0d08f754f89a4c6a029d8dd47e"
  (:require [clojure.spec :as s]
            [clojure.test :as t]))


(def ^:dynamic *conform-mode* nil)

(defn string->int [x]
  (if (string? x)
    (try
      (Integer/parseInt x)
      (catch Exception _
        :clojure.spec/invalid))))

(defn string->long [x]
  (if (string? x)
    (try
      (Long/parseLong x)
      (catch Exception _
        :clojure.spec/invalid))))

(defn string->double [x]
  (if (string? x)
    (try
      (Double/parseDouble x)
      (catch Exception _
        :clojure.spec/invalid))))

(defn string->keyword [x]
  (if (string? x)
    (keyword x)))

(defn string->boolean [x]
  (if (string? x)
    (cond
      (= "true" x) true
      (= "false" x) false
      :else :clojure.spec/invalid)))

(def +string-conformers+
  {:utilza.coercions/int string->int
   :utilza.coercions/long string->long
   :utilza.coercions/double string->double
   :utilza.coercions/keyword string->keyword
   :utilza.coercions/boolean string->boolean})

(def +conform-modes+
  {:utilza.coercions/string [string? +string-conformers+]})

(defn dynamic-conformer [accept? type]
  (with-meta
    (s/conformer
     (fn [x]
       (if (accept? x)
         x
         (if-let [[accept? conformers] (+conform-modes+ *conform-mode*)]
           (if (accept? x)
             ((type conformers) x)
             ;; XXX workaround/hack for http://dev.clojure.org/jira/browse/CLJ-1966
             (keyword "clojure.spec" "invalid"))
           (keyword "clojure.spec" "invalid")))))
    {:utilza.coercions/type type}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Unit Tests

(defn test-defs
  [f]
  
  ;; Type'ish
  (def aInt (dynamic-conformer integer? :utilza.coercions/int))
  (def aBool (dynamic-conformer boolean? :utilza.coercions/boolean))
  (def aLong (dynamic-conformer boolean? :utilza.coercions/long))
  (def aKeyword (dynamic-conformer boolean? :utilza.coercions/keyword))

  ;; Schema
  (s/def :utilza.coercions/age (s/and aInt #(> % 10)))
  (s/def :utilza.coercions/truth aBool)
  (s/def :utilza.coercions/over-million (s/and aLong #(> % 1000000)))
  (s/def :utilza.coercions/language (s/and aKeyword #{:clojure :clojurescript}))

  (f))


(t/use-fixtures :once test-defs)

(t/deftest without-coercion
  (t/is (= (s/conform :utilza.coercions/age "12") :clojure.spec/invalid))
  (t/is (= (s/conform :utilza.coercions/truth "false") :clojure.spec/invalid))
  (t/is (= (s/conform :utilza.coercions/over-million "1234567") :clojure.spec/invalid))
  (t/is (= (s/conform :utilza.coercions/language "clojure") :clojure.spec/invalid)))


(t/deftest with-coercion
  (binding [*conform-mode* :utilza.coercions/string]
    (t/is (= (s/conform :utilza.coercions/truth "false") false))
    (t/is (= (s/conform :utilza.coercions/over-million "1234567") 1234567))
    (t/is (= (s/conform :utilza.coercions/language "clojure") :clojure))
    (t/is (= (s/conform :utilza.coercions/age "12") 12))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  (t/run-tests)

  
  )
