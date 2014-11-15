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

(def regs {:mcp08/iodir	        0x00
           :mcp08/ipol	        0x01
           :mcp08/gpinten	0x02
           :mcp08/defval	0x03
           :mcp08/intcon	0x04
           :mcp08/iocon	        0x05
           :mcp08/gppu	        0x06
           :mcp08/intf	        0x07
           :mcp08/intcap	0x08
           :mcp08/gpio	        0x09
           :mcp08/olat	        0x0a})


(def bits (apply g/bit-seq (repeat 8 1))) 

(defn open-bus
  [id]
  (log/info "opening dev " id)
  (let [fd (libc/open (format "/dev/i2c-%d" id) (byte (:rdwr flags)))]
    (if (pos? fd)
      fd
      ;; TODO: get jna/when-err going instead.
      (log/error (jna/invoke String c/strerror (Native/getLastError))))))

(defn close-bus
  [id]
  (libc/close id))

(defn set-address!
  [fd addr]
  (libc/ioctl fd (-> flags :slave int) addr))



(defn read-reg
  "Takes an integer fd, and a keyword with the name of the register,
   reads the register, and returns the value as a byte"
  [fd reg]
  (jna/invoke Integer smbus/smbus_read_byte_data fd (-> regs reg byte)))



(defn read-decode-reg
  [fd reg]
  (gio/decode bits (->> reg
                        (read-reg fd)
                        vector
                        byte-array)))

(defn write-reg!
  [fd reg b]
  (jna/invoke Integer smbus/smbus_write_byte_data fd (-> regs reg byte) b))

(defn write-encode-reg!
  [fd reg bitseq]
  (write-reg! fd reg (.get (gio/contiguous (gio/encode bits bitseq)))))




(defn falling-edge-pins
  "Takes a vector of previous pin states, and current pin states.
   Returns a seq of bit numbers which are falling"
  [prev cur]
  (for [[i v] (map-indexed vector (reverse (map #(and %1 (not %2)) prev cur)))
        :when v]
    i))





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment


  (def i2c (open-bus 1))
  (set-address! i2c 0x20)

  (write-encode-reg! i2c :mcp08/gpio [0 0 0 0 1 1 1 1])
  
  (close-bus i2c)

  
  )
