(ns utilza.pom
  (:use [cemerick.pomegranate :only (add-dependencies)]))

(defn install [x]
  "wrapper around pomegranate"
  (add-dependencies
   :coordinates x
   :repositories (merge cemerick.pomegranate.aether/maven-central
                        {"clojars" "http://clojars.org/repo"})))

