;;; utilities related to hiccup and html formattingx

(ns utilza.hiccupy
  (:require [clojure.string :as string]))


(defn anchorify
  "Make anchors"
  [s]
  (string/replace s #"[^a-zA-Z]" ""))


(defn tabify
  "Make nice headers for jquery tabs. Call (html) on it to format it."
  [m]
  [:ul
   (for [x m]
     [:li
      [:a {:href (or (:href x) (->> x  :title anchorify (str "#")))}
       (:title x)]])]
  (for [x m]
    [:div {:id (-> x :title anchorify )} (:body x)]))




(defn tablify
  "Make html table from vector of vectors"
  [vv]
  [:table  
   (for [r vv]
     [:tr
      (for [i r]
        [:td i])])])





