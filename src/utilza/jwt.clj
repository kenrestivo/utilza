(ns utilza.jwt
  "Deal with Google's weird JWT thing"
  (:require  [utilza.json :as json])
  (:import com.google.api.client.json.jackson.JacksonFactory
           com.google.api.client.json.webtoken.JsonWebSignature))



(defn decode-jwt
  [s]
  (->> s
       (JsonWebSignature/parse (JacksonFactory.))
       .getPayload
       .toString
       json/decode))

