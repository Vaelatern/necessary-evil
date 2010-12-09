(ns necessary-evil.methodresponse
  "This module implementents the methodResponse wire format portion of the XML-RPC
   spec. You can find the official spec at http://www.xml-rpc.com/spec.

   A methodResponse may contain either a value or a fault."
  (:use [necessary-evil.value :only [parse-value value-elem]]
        [necessary-evil.xml-utils])
  (:require [clojure.xml :as xml]))

;; returning a Fault record from any piece of rpc handler will result
;; in a fault methodResponse being generated. Anything else will
;; generate a params blob

(defrecord Fault [^Integer fault-code ^String fault-message])

;; serialising methodResponses to XML

(defn value-response
  "method-response generates xml for a successful method call."
   [value]
   (elem :params [(elem :param [(value-elem value)])]))

(defn fault-response
  "fault-response generates xml for a failed method call"
  [fault-code fault-message]
  (elem :fault [(value-elem {:faultCode   (str fault-code)
                             :faultString (str fault-message)})]))

(defprotocol ResponseElements
  "This protocol is used to generate the correct nodes for a value or a fault."
  (response-elem [v]))

(extend-protocol ResponseElements
  Fault
  (response-elem [fault] (fault-response (:fault-code fault) (:fault-message fault)))

  Object
  (response-elem [v] (value-response v)))


(defn emit-method-response
  "returns wraps the content nodes in the method-response elements"
  [value]
  (xml/emit (elem :methodResponse [(response-elem value)])))

;; deserialising methodResponse XML to clojure structures

