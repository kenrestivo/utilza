(ns utilza.spec
  (:require [clojure.spec :as s]))

(s/def :utilza/pos-int (s/and integer? pos?))


(defn validate-data
  "Validates settings based on setting-spec supplied. Throws ex-info if fails."
  [settings-spec settings]
  (if (s/valid? settings-spec settings)
    (s/conform settings-spec settings)
    (throw (ex-info "Invalid settings" (s/explain-data settings-spec settings)))))


(defn validate
  "Validates settings based on setting-spec supplied. Prints any errors to stdout"
  [settings-spec settings]
  (if (s/valid? settings-spec settings)
    (s/conform settings-spec settings)
    (do
      ;; XXX hack. For some reason, explain doesn't print to stdout in repl, so use println.
      (println (s/explain-str settings-spec settings))
      (throw (ex-info "Invalid settings" (s/explain-data settings-spec settings))))))
