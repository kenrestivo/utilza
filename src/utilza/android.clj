(ns android
  (:require [neko.context :as context])
  (:import android.os.Handler
           android.os.HandlerThread
           android.content.ComponentName
           android.content.pm.PackageInfo
           android.content.Intent))

(defn periodic
  "Executes function f at interval milliseconds, named with name.
   Returns the handlerthread to use for cancellation (.quit ht)"
  [f interval name]
  (let [ht  (-> name
                HandlerThread.
                (doto .start))
        h (-> ht
              .getLooper
              Handler.)]
    (letfn [(hf [] (f) (.postDelayed h hf interval))]
      (.postDelayed h hf interval))
    ht))


(defn start-activity
  "Starts a new activity. pkg-name is the package part of the component,
   activity-name is the non-package part of the name. i.e. [\"org.foobar.app\" \"SomeActivity\"]"
  [^String pkg-name activity-name]
  (.startActivity context/context
                  (doto (Intent.)
                    (.setAction Intent/ACTION_MAIN)
                    (.addFlags Intent/FLAG_ACTIVITY_NEW_TASK)
                    (.setComponent  (ComponentName. pkg-name (str pkg-name "." activity-name))))))


(defn get-version-info
  "Returns a map of :version-name and :version-number for given package-name"
  [^String package-name]
  (let [^PackageInfo pi (-> context/context
               .getPackageManager
               (.getPackageInfo package-name 0))]
    {:version-name (.versionName pi)
     :version-number (.versionCode pi)}))