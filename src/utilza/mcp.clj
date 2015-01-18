(ns utilza.mcp
  (:require [net.n01se.clojure-jna :as jna]
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

           :mcp17 {:iodir	0x00
                   :ipol	0x02
                   :gpinten	0x04
                   :defval	0x06
                   :intcon	0x08
                   :iocon	0x0a
                   :gppu	0x0c
                   :intf	0x0e
                   :intcap	0x10
                   :gpio	0x12
                   :olat	0x14}})

(defn wordify
  "Takes a keyword. Returns a pair of the keyword with and b appended.
   Used for converting :iodir to [:iodira :iodirb]"
  [kw]
  (for [s ["a" "b"]]
    (keyword (str (name kw) s))))


(defn open-bus
  [id]
  (log/info "opening dev " id)
  (let [fd (libc/open (format "/dev/i2c-%d" id) (byte (:rdwr flags)))]
    (if (pos? fd)
      (int fd)
      ;; TODO: get jna/when-err going instead.
      (throw (Exception. (jna/invoke String c/strerror (Native/getLastError)))))))

(defn close-bus
  [fd]
  (libc/close fd))

(defn set-address!
  [fd addr]
  ;; TODO: error checking
  (libc/ioctl (int fd) (-> flags :slave int) addr))



(defn read-reg
  "Takes an integer fd, and a keyword with the name of the register,
   reads the register, and returns the value as a byte"
  [fd dev-type reg]
  ;; TODO: do the case dispatch after invoke
  (case dev-type
    :mcp08 (jna/invoke Integer smbus/smbus_read_byte_data (int fd) (-> regs dev-type reg unchecked-byte))
    :mcp17 (jna/invoke Integer smbus/smbus_read_word_data (int fd) (-> regs dev-type reg unchecked-byte))
    (throw (Exception. (str "incorrect type" dev-type)))))



(defn reg-value->bit-seq
  "Takes a register value x and a device type.
   Return a sequence of bits representing the register's value"
  [x dev-type]
  (vec (reverse (for [i (range (case dev-type
                                 :mcp08 8
                                 :mcp17 16))]
               (if (bit-test x i) 1 0)))))


(defn read-decode-reg
  [fd dev-type reg]
  (reg-value->bit-seq (read-reg fd dev-type reg) dev-type))


(defn write-reg!
  [fd dev-type reg b]
  (case dev-type
    :mcp08 (jna/invoke Integer smbus/smbus_write_byte_data (int fd)
                       (-> regs dev-type reg unchecked-byte)
                       (-> b (bit-and 0xff) unchecked-byte))
    :mcp17 (jna/invoke Integer smbus/smbus_write_word_data (int fd)
                       (-> regs dev-type reg unchecked-byte)
                       (-> b (bit-and 0xffff) unchecked-short))
    (throw (Exception. (str "incorrect type" dev-type)))))




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
  (when (and prev cur) ;; remove nils
    (into {} (filter second (map-indexed vector (reverse (map (fn [p c]
                                                                (condp #(%1 (apply - %2)) [c p]
                                                                  pos? :rising
                                                                  neg? :falling
                                                                  = false))
                                                              prev cur)))))))

(defn bit-subset
  "Takes a seq of bit indexes, and a vector of bits representing a register value.
    Returns a vector with only the selected bits, in order of the bit indexes presented"
  [selected-bit-positions bit-values]
  (reverse (vals (select-keys (vec (reverse bit-values)) selected-bit-positions))))


(defn bitpos-to-bytes
  [bit-positions]
  (* 8 (Math/ceil (double (/ (+ (apply max bit-positions) 1) 8)))))

(defn bitpos-to-bitmap
  "Takes a collection of bit-positions i.e. [2 3 7 11] and converts
   it to a bitmap seq of bits. Calculates size of bitmap from the maximum of bit-positions"
  ([bit-positions]
     (bitpos-to-bitmap bit-positions (bitpos-to-bytes bit-positions)))
  ([bit-positions size]
     (reverse (reduce #(assoc %1 %2 1) (vec (repeat size 0)) bit-positions))))


(defn set-pin
  "Sets bit bit-num in bit-seq to value.
  bit-seq bits are numbered right to left."
  [bit-seq bit-num value]
  {:pre [(not (nil? bit-seq))
         (<= bit-num (count bit-seq))]}
  (log/trace "set-pin" bit-seq bit-num value)
  (reverse (assoc (vec (reverse bit-seq)) bit-num value)))



(defn bitpos-to-num
  "Takes collection of set bit positions,
   returns a long with those bits set"
  [bit-positions]
  (reduce #(bit-set %1 %2) 0 bit-positions))


(defn changed-bits
  [prev cur input-mask dev-type]
  (apply changed-pins (for [r [prev cur]]
                        (-> r
                            (bit-and input-mask)
                            (reg-value->bit-seq dev-type)))))

