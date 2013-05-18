(defproject utilza "0.1.13"
  :description "ken's random web-related utilities"
  :url "https://github.com/kenrestivo/utilza"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :dev-dependencies [
                     [noir "1.3.0-beta7"]
                     [clj-http "0.4.1"]
                     [com.ashafa/clutch "0.3.0"]
                     [org.timmc.handy "1.5.0"]]
  :plugins [[lein-cljsbuild "0.3.2" ]]
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

