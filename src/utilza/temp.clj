(ns utilza.temp)


(defn c->f
  "Convert celsius to farenheit for the USA"
  [c]
  (-> c
      (* 9/5)
      (+ 32.0)
      float))



(defn f->c
  "Conver farenheit to celsius"
  [f]
  (-> f
      (- 32.0)
      (* 5/9)
      float))
