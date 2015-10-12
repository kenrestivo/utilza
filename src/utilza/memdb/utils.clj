(ns utilza.memdb.utils
  (:require [clojure.core.reducers :as r]))


(defn update-record
  "Returns a function to update the db by applying f to the record at id.
   Suitable for use with swap!"
  [id f]
  (fn [db]
    (update-in db [id] f)))


(defn all-keys
  [db k]
  (reduce #(conj %1 %2)
          #{}
          (r/map k (-> db vals))))

(defn rinc
  [c _]
  (inc c))


(defn key-set-counts
  "Gets counts for all unique records in db which satisfy key function."
  [db k]
  (->> db
       vals
       (map k)
       frequencies
       (sort-by second)
       reverse))


(defn total-not-null-counts
  "Finds total count of all records with not-nul key satisfying function k."
  [db k]
  (reduce rinc 0 (r/filter k (vals db))))


(defn simple-contains
  [db k s]
  (->> (reduce #(conj %1 %2)
               []
               (->> db
                    vals
                    (r/filter #(some-> % k (.contains s)))))
       (sort-by :time)))