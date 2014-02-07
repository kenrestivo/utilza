(ns utilza.base32
  (:import  org.apache.commons.codec.binary.Base32))


(defn str->base32s
  [s]
  (->> s
       .getBytes
       (.encodeAsString (Base32.))))

(defn base32->bytes
  [b32s]
  (->> b32s
       .getBytes
       (.decode (Base32.))))

(defn base32->str
  [b32s]
  (->> b32s
       base32->bytes
       String.))



(comment

  (str->base32s "foobar")
  ;; also works for the way back
  (base32->str "MZXW6YTBOI")

  (->>  "MZXW6YTBOI"
        base32->bytes
        seq)
  
  )


