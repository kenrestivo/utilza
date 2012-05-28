;;; utilities related to couchdb/clutch

(ns utilza.clutch
  (:require [com.ashafa.clutch :as clutch]))


(defn save-or-update
  "Silently overwrite/update a clutch doc if it is already present"
  [db m]
  (if-let [found (clutch/get-document db (:_id m))]
    (clutch/update-document db found m)
    (clutch/put-document db m)))