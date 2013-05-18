(ns utilza.utils
  (:require [goog.net.XhrIo :as xhr]))
  

;; some basic utlities for when shoreleave isn't necessary


(defn log [s] (.log js/console s))



(defn make-receiver
  "success takes text as arg.
   failure takes response as args"
  [success failure]
  (fn  [event]
    (let [response (.-target event)
          txt (.getResponseText response)]
      (if (.isSuccess response)
        (success txt)
        (failure response)))))


(defn xhr-get
  ([url success fail]
     (xhr-get url (make-receiver  success fail)))
  ([url cb]
     (xhr/send url cb "GET" nil nil 0)))



(defn xhr-post-json
  ([url data success fail]
     (xhr-post-json url (make-receiver  success fail) data))
  ([url cb data]
     (xhr/send url cb "POST"
               (.stringify js/JSON data)
               (clj->js {"Content-Type" "application/json"})
               0)))


(defn xhr-post-edn
  ([url data success fail]
     (xhr-post-edn url (make-receiver  success fail) data))
  ([url cb data]
     (xhr/send url cb "POST"
               (pr-str data)
               (clj->js {"Content-Type" "application/edn"})
               0)))




(defn ready [f]
  (.ready (js/jQuery js/document) f))

