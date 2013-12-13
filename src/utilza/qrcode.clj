(ns qrcode
  (:import 
   javax.imageio.ImageIO
   [com.google.zxing.client.j2se
    BufferedImageLuminanceSource]
   [com.google.zxing
    BinaryBitmap
    ReaderException]
   [com.google.zxing.common
    HybridBinarizer
    BitMatrix]
   com.google.zxing.qrcode.QRCodeReader))



(defn qr-decode
  [fname]
  (with-open [i (clojure.java.io/input-stream fname)]
    (->> i
         ImageIO/read
         BufferedImageLuminanceSource.
         HybridBinarizer.
         BinaryBitmap.
         (.decode (QRCodeReader.))
         .toString)))


(comment

  (qr-decode "/tmp/foo.jpg")

  )

;; MCCDatabaseMismatchException
