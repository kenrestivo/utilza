(ns utilza.utils)


(defn anchorify
  "Make anchors"
  [s]
  (string/replace s #"[^a-zA-Z]" ""))


(defn tabify
  "Make nice for jquery tabs"
  [m]
  (html [:ul
       (for [x m]
         [:li
          [:a {:href (->> x  :title anchorify (str "#"))} (:title x)]])]
        (for [x m]
          [:div {:id (-> x :title anchorify )} (:body x)])))


