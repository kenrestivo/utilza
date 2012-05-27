;;; A simple mini-library to persist noir session memory store
;;; across reboots of the JVM.
;;; Useful for sites where it doesn't make sense to store session persistently,
;;; but you don't want to make users log back in whenever you push to Heroku,
;;; which restarts the JVM and loses its memory store.

(ns utilza.noirutils.session
  (:require [com.ashafa.clutch :as clutch]))


(def clutch-keys #{:_id :_rev})


(defn- decide-key
  "Use a default key if none given."
  [kk]
  (or (first kk) :session-persist))
  
  
(defn save!
  "Saves Noir memory session store to persistent clutch DB.
   Give it an optional key to use, :session-persist is default
   This is destructive: you don't want to keep ancient keys around,
   so it'll wipe out the old doc and replace it with the current memory store.
   Call it from inside clutch/with-db"
  [& kk]
  (let [k (decide-key kk)]
    (if-let [doc  (clutch/get-document k)]
      (clutch/update-document (select-keys doc clutch-keys) @noir.session/mem)
      (clutch/put-document @noir.session/mem :id k))))


(defn restore!
  "Restores memory session store contents, from persistent storage.
   Give it an optional key to use, :session-persist is default
   Note: keeps new keys, but clobbers any duplicates using the restored values.
   Call it from inside clutch/with-db"
  [& kk]
  (let [k (decide-key kk)]
    (swap! noir.session/mem
           merge
           (apply (partial dissoc (clutch/get-document  k))
                  clutch-keys))))


(defn wipe!
  "Just removes the persistent store, to start over if needed.
   Give it an optional key to use, :session-persist is default
   Call it from inside clutch/with-db"
  [& kk]
  (let [k (decide-key kk)]
    (when-let [doc  (clutch/get-document k)]
      (clutch/delete-document doc))))


(comment ;; example
  (def db (clutch/get-database
           (com.ashafa.clutch.utils/url "session-restore-test")))
  ;; call this from a private url handler, perhaps, before pushing or restarting
  (with-db db (save!))
  ;; restart the jvm, push a new slug to heroku, etc
  ;; put this in your server/-main method somewhere:
  (with-db db (restore!))
  ;; in case the list grows too long, sessions don't expire, or
  ;; you just don't want to keep old sessions around anymore
  (with-db db (wipe!)))
  