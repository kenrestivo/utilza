;;; utilities related to hiccup and html formattingx

(ns utilza.hiccupy
  (:require [clojure.string :as string]))


(defn anchorify
  "Make anchors"
  [s]
  (string/replace s #"[^a-zA-Z]" ""))


(defn tabify
  "Make nice headers for jquery tabs. Call (html) on it to format it.
   ms is a seq of maps. Each map has :href for URL, and :title for title "
  [ms]
  [:ul
   (for [x ms]
     [:li
      [:a {:href (or (:href x) (->> x  :title anchorify (str "#")))}
       [:span (:title x)]]])]
  (for [x m]
    [:div {:id (-> x :title anchorify )}]))




(defn tablify
  "Make html table from vector of vectors"
  [vv]
  [:table  
   (for [r vv]
     [:tr
      (for [i r]
        [:td i])])])





