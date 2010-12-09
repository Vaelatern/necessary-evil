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

(defprotocol ResponseElements
  "This protocol is used to generate the correct nodes for a value or a fault."
  (response-elem [v]))

(extend-protocol ResponseElements
  Fault
  (response-elem
   [fault] (elem :fault [(value-elem {:faultCode   (str (:fault-code fault))
                                      :faultString (str (:fault-message fault))})]))

  Object
  (response-elem [value] (elem :params [(elem :param [(value-elem value)])])))


(defn emit-method-response
  "returns wraps the content nodes in the method-response elements"
  [value]
  (xml/emit (elem :methodResponse [(response-elem value)])))

;; deserialising methodResponse XML to clojure structures

