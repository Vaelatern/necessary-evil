(ns necessary-evil.methodresponse
  "This module implementents the methodResponse wire format portion of the XML-RPC
   spec. You can find the official spec at http://www.xml-rpc.com/spec.

   A methodResponse may contain either a value or a fault."
  (:use [necessary-evil.value :only [parse-value value-elem]]
        [necessary-evil.xml-utils])
  (:require [clojure.xml :as xml]))


;; serialising methodResponses to XML

(defn method-response-elem
  "returns wraps the content nodes in the method-response elements"
  [content-nodes]
  (elem :methodResponse [content-nodes]))


(defn emit-method-response
  "emit-method-response generates xml for a successful method call."
  [value]
  (let [param-content (value-elem value)]
    (xml/emit (method-response-elem (elem :params [(elem :param [param-content])])))))

(defn emit-fault-response
  "emit-fault-response generates xml for a failed method call"
  [fault-code fault-message]
  (let [fault-content (value-elem {:faultCode (int fault-code)
                                   :faultString (str fault-message)})]
    (xml/emit (method-response-elem (elem :fault [fault-content])))))


;; deserialising methodResponse XML to clojure structures
