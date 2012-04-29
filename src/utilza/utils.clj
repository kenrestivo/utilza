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
          [:a {:href (or (:href x) (->> x  :title anchorify (str "#")))}
           (:title x)]])]
        (for [x m]
          [:div {:id (-> x :title anchorify )} (:body x)])))




(defn map-vals
  "Apply a function to all the values (thanks technomancy)"
  [f m]
  (zipmap (keys m) (map f (vals m))))


;; TODO: make this into a macro, so server/start doesn't have to be pulled in
(defn manual []
  (let [mode :dev
        port 8081]
    (server/start port
                  {:mode mode
                   :ns (-> (.split #"\." (.toString *ns*)) first symbol )})))


;;; general couch stuff

(defn save-or-update
  "Silently overwrite a clutch doc if it already present"
  [db m]
  (if-let [found (clutch/get-document db (:_id m))]
    (clutch/update-document db found m)
    (clutch/put-document db m)))


;;; file sysstem stuff

(defn intermediate-paths
  "Takes file-path, a complete path to the file, including the file iteself!
    Strips off the filename, returns a list of intermediate paths.
    Thanks to emezeke for the reductions device."
  [file-path]
  (let [paths (pop (split file-path  #"\/"))]
    (map #(clojure.string/join "/" %) (rest (reductions conj [] paths)))))

