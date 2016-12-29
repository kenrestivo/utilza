(ns utilza.time
  (:require
   [clj-time.format :as f]
   [clj-time.core :as t]))


(defn days-ago
  "Returns string of time x days ago in a kibana-like format"
  [x]
  (f/unparse (f/formatters :date-hour-minute-second)   (t/minus (t/now) (t/days x))))


(defn minutes-ago
  "Returns string of time x minutes ago in a kibana-like format"
  [x]
  (f/unparse (f/formatters :date-hour-minute-second)   (t/minus (t/now) (t/minutes x))))

(defn yesterday
  "Returns string of 1 day ago in kibana-like format"
  []
  (days-ago 1))
