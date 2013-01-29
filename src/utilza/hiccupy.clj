;;; utilities related to hiccup and html formattingx

(ns utilza.hiccupy
  (:require [clojure.string :as string]))


(defn anchorify
  "Make string safe for usage as an HTML anchor,
   by removing all the non-alpha chars.
   WARNING: you could end up with a collision here."
  [s]
  (string/replace s #"[^a-zA-Z]" ""))


(defn tabify
  "Make nice headers for jquery tabs. Call (html) on it to format it.
   ms is a seq of maps. Each map has :href for URL, and :title for title "
  [ms]
  [:ul
   (for [m ms]
     [:li
      [:a {:href (or (:href m) (->> m  :title anchorify (str "#")))}
       [:span (:title m)]]])]
  (for [m ms]
    [:div {:id (-> m :title anchorify )}]))




(defn tablify
  "Make html table from vector of vectors,
  i.e [[r1-c1 r1-c2 r1-c3] [r2-c1 r2-c2 r2-c3] ...  ]"
  [vv]
  [:table  
   (for [r vv]
     [:tr
      (for [i r]
        [:td i])])])





