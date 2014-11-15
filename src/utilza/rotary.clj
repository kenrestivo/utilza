(ns utilza.rotary)

;; Utility functions for dealing with rotary encoders

(def rotary-phases {[0 0] {:prev [1 0]
                           :next [0 1]}
                    [0 1] {:prev [0 0]
                           :next [1 1]}
                    [1 1] {:prev [0 1]
                           :next [1 0]}
                    [1 0] {:prev [1 1]
                           :next [0 0]}})

(defn rotary-dir
  "Takes vectors of bits, for prev and current position.
   Returns :left if transition was left, :right if transition was right, and nil if invalid"
  [prev cur]
  (let [d (get rotary-phases cur)]
    (condp = prev
      (:prev d) :right
      (:next d) :left
      nil)))

