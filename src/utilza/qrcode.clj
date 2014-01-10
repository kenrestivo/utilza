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


(defn image-to-stream
  "Turns an image into something that ring can deal with"
  [img]
  (let [os (ByteArrayOutputStream.)]
    (ImageIO/write img (:type settings) os)
    (.toByteArray os)))


(defn decode
  [fname]
  (with-open [i (clojure.java.io/input-stream fname)]
    (->> i
         ImageIO/read
         BufferedImageLuminanceSource.
         HybridBinarizer.
         BinaryBitmap.
         (.decode (QRCodeReader.))
         .toString)))


(defn encode
  "Takes string and size , and returns the bytes of a QRcode image of it of size.
   Transliterated from http://www.javacodegeeks.com/2012/10/generate-qr-code-image-from-java-program.html"
  [s size]
  (let [bm (.encode (QRCodeWriter.)
                    s BarcodeFormat/QR_CODE
                    size size
                    (doto (Hashtable.)
                      (.put EncodeHintType/ERROR_CORRECTION ErrorCorrectionLevel/H)))
        w (.getWidth bm)
        img (BufferedImage. w w BufferedImage/TYPE_INT_RGB)
        s2d (.createGraphics img)]
    (doto s2d
      (.setColor Color/WHITE)
      (.fillRect 0 0 w w )
      (.setColor Color/BLACK))
    (doseq [[x y] (range-2d 0 w)]
      (when (.get bm x y)
        (.fillRect s2d x y 1 1)))
    (.dispose s2d) ;; clean up after ourselves, just to avoid potential leaks
    (image-to-stream img)))



(comment

  (qr-decode "/tmp/foo.jpg")

  (->> "http://github.com/kenrestivo/utilza"
       encode
       (spit "/tmp/foo.jpg"))
  )




;; MCCDatabaseMismatchException
