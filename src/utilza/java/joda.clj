(ns utilza.java.joda
  "Joda-specific"
  (:import org.joda.time.DateTime))



(defn date-range
  "Generates lazy seq of range of dates incremented by 1 day"
  [start end]
  (take-while #(or (.isBefore %  end) (= % end))
              (iterate #(.plusDays % 1) (DateTime. start))))