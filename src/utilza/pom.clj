(ns utilza.pom
  (:use [cemerick.pomegranate :only (add-dependencies)]))

(defn install [x]
  "wrapper around pomegranate"
  (add-dependencies
   :coordinates x
   :repositories (merge {"central" {:url "http://repo1.maven.org/maven2/"
                                    :checksum :warn}}
                        {"clojars" {:url "http://clojars.org/repo"
                                    :checksum :warn}})))