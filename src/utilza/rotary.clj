(ns utilza.rotary)

;; Utility functions for dealing with rotary encodersg

(def rotary-phases {[false false] {:prev [true false]
                                   :next [false true]}
                    [false true] {:prev [false false]
                                  :next [true true]}
                    [true true] {:prev [false true]
                                 :next [true false]}
                    [true false] {:prev [true true]
                                  :next [false false]}})

(defn rotary-dir
  "Takes vectors of bits, for prev and current position.
   Returns :left if transition was left, :right if transition was right, and nil if invalid"
  [prev cur]
  (let [d (get rotary-phases cur)]
    (condp = prev
      (:prev d) :right
      (:next d) :left
      nil)))

