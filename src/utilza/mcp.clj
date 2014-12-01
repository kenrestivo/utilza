(ns utilza.mcp
  (:require [net.n01se.clojure-jna :as jna]
            [gloss.core :as g]
            [gloss.io :as gio]
            [gloss.core.formats :as fmt]
            [taoensso.timbre :as log])
  (:import (com.sun.jna Native Library)))


;; Utility functions for interfacing with a MCP i2c port expander chip

(jna/to-ns libc c  [Integer ioctl
                    Integer read
                    Integer write
                    Integer open
                    Integer close])


;; Flags defined in i2c-dev.h
;; TODO: add the rest of them
(def flags {:rdwr           0x0002
            :slave          0x0703
            :slave-force    0x0706})

(def regs {:mcp08 {:iodir	0x00
                   :ipol	0x01
                   :gpinten	0x02
                   :defval	0x03
                   :intcon	0x04
                   :iocon	0x05
                   :gppu	0x06
                   :intf	0x07
                   :intcap	0x08
                   :gpio	0x09
                   :olat	0x0a}

           :mcp17 {:iodira      0x00
                   :ipola       0x01
                   :gpintena	0x02
                   :defvala	0x03
                   :intcona	0x04
                   :iocona	0x05
                   :gppua	0x06
                   :intfa	0x07
                   :intcapa	0x08
                   :gpioa	0x09
                   :olata	0x0a
                   :iodirb      0x10
                   :ipolb       0x11
                   :gpintenb	0x12
                   :defvalb	0x13
                   :intconb	0x14
                   :ioconb	0x15
                   :gppub	0x16
                   :intfb	0x17
                   :intcapb	0x18
                   :gpiob	0x19
                   :olatb	0x1a
                   }})


(defn wordify
  "Takes a keyword. Returns a pair of the keyword with and b appended.
   Used for converting :iodir to [:iodira :iodirb]"
  [kw]
  (for [s ["a" "b"]]
    (keyword (str (name kw) s))))

(def bits (apply g/bit-seq (repeat 8 1))) 


(defn open-bus
  [id]
  (log/info "opening dev " id)
  (let [fd (libc/open (format "/dev/i2c-%d" id) (byte (:rdwr flags)))]
    (if (pos? fd)
      (int fd)
      ;; TODO: get jna/when-err going instead.
      (throw (Exception. (jna/invoke String c/strerror (Native/getLastError)))))))

(defn close-bus
  [id]
  (libc/close id))

(defn set-address!
  [fd addr]
  ;; TODO: error checking
  (libc/ioctl (int fd) (-> flags :slave int) addr))



(defn read-reg
  "Takes an integer fd, and a keyword with the name of the register,
   reads the register, and returns the value as a byte"
  [fd dev-type reg]
  ;; TODO: handle 16-bit
  (jna/invoke Integer smbus/smbus_read_byte_data (int fd) (-> regs dev-type reg byte)))

(defn read-decode-reg
  [fd dev-type reg]
  ;; TODO: handl 16-bit
  (gio/decode bits (->> reg
                        (read-reg fd dev-type)
                        vector
                        byte-array)))

(defn write-reg!
  [fd dev-type reg b]
  ;; TODO: handle 16-bit
  (jna/invoke Integer smbus/smbus_write_byte_data (int fd) (-> regs dev-type reg byte) b))


(defn write-encode-reg!
  [fd dev-type reg bitseq]
  ;; TODO: handle 16-bit
  (write-reg! fd dev-type reg  (.get (gio/contiguous (gio/encode bits bitseq)))))





(defn falling-edge-pins
  "Takes a vector of previous pin states, and a vector of current pin states.
   Returns a seq of bit numbers which are falling"
  [prev cur]
  (for [[i v] (map-indexed vector (reverse (map #(and %1 (not %2)) prev cur)))
        :when v]
    i))

(defn changed-pins
  "Takes a vector of previous pin states (1 or 0), and a vector of current pin states.
   Returns a map of bit numbers as keys, and state :rising :falling"
  [prev cur]
  (into {} (filter second (map-indexed vector (reverse (map (fn [p c]
                                                              (condp #(%1 (apply - %2)) [c p]
                                                                pos? :rising
                                                                neg? :falling
                                                                = false))
                                                            prev cur))))))

(defn bit-subset
  "Takes vector of booleans, and a seq of bit indexes.
    Returns a vector with the bits"
  [pins bs]
  (reverse (vals (select-keys (vec (reverse bs)) pins))))


(defn bitpos-to-bytes
  [bit-positions]
  (* 8 (Math/ceil (double (/ (+ (apply max bit-positions) 1) 8)))))

(defn bitpos-to-bitmap
  "Takes a seq of bit-positions i.e. [2 3 7 11] and converts
   it to a bitmap seq of bits. Calculates size of bitmap from the maximum of bit-positions"
  ([bit-positions]
     (bitpos-to-bitmap bit-positions (bitpos-to-bytes bit-positions)))

  ([bit-positions size]
     (reverse (reduce #(assoc %1 %2 1) (vec (repeat size 0)) bit-positions))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment


  (def i2c (open-bus 1))
  (set-address! i2c 0x20)

  (write-encode-reg! i2c :mcp08 :gpio [0 0 0 0 1 1 1 1])
  
  (close-bus i2c)

  
  )
