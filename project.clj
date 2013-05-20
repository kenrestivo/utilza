(defproject utilza "0.1.16"
  :description "ken's random web-related utilities"
  :url "https://github.com/kenrestivo/utilza"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :dev-dependencies [
                     [noir "1.3.0-beta7"]
                     [clj-http "0.4.1"]
                     [org.timmc.handy "1.5.0"]
                     [com.ashafa/clutch "0.3.0"]
                     ;; postgres-related
                     [org.slf4j/slf4j-log4j12 "1.6.4"] ;; clown shoes
                     [postgresql "9.1-901-1.jdbc4"]
                     [org.clojure/java.jdbc "0.3.0-alpha1"]
                     [c3p0/c3p0 "0.9.1.2"]
                     [honeysql "0.3.0"]]
  :plugins [[lein-cljsbuild "0.3.2" ]]
  :source-paths ["src" "src-cljs"]
  :cljsbuild {:builds
              [{:id "dev",
                :source-paths ["src-cljs"],
                :compiler
                {:pretty-print true,
                 :output-to "resources/public/js/main-dev.js",
                 :optimizations :whitespace},
                :jar true}
               {:id "production",
                :source-paths ["src-cljs"],
                :compiler
                {:pretty-print false,
                 :output-to "resources/public/js/main.js",
                 :externs ["externs/jquery-1.8.js"
                           "externs/twitter-bootstrap.js"],
                 :optimizations :advanced},
                :jar true}]}
  :aliases {
            ;; simple, for thrashing the server
            "tr" ["with-profile" "user,dev,server"
                  "trampoline" "repl" ":headless"]
            ;; for production, don't need cljsbuild auto in this one,
            ;; and better trampoline for memory usage.
            "autorepl" ["with-profile" "user,dev,server"
                        "pdo"
                        "cljsbuild" "auto" "dev,"
                        "repl" ":headless"]
            "prod" ["with-profile" "user,server"
                    "do"
                    "trampoline" "repl" ":headless"]})

