(ns android
  (:import android.os.Handler
           android.os.HandlerThread))

(defn periodic
  "Executes function f at interval milliseconds, named with name.
   Returns the handlerthread to use for cancellation (.quit ht)"
  [interval name f]
  (let [ht  (-> name
                HandlerThread.
                (doto .start))
        h (-> ht
              .getLooper
              Handler.)]
    (letfn [(hf []
              (f)
              (.postDelayed h hf interval))]
      (.postDelayed h hf interval))
    ht))
