(ns necessary-evil.methodcall
  "This module implements the methodCall wire format portion of the
   XML-RPC spec. You can find the official spect at
   http://www.xml-rpc.com/spec.

   It is expected that this module will always be required, rather than used.
   The parse function will consume a clojure.xml structure and return a
   MethodCall record. The unparse function is the dual of parse; it takes a
   MethodCall record and returns xml.

   Use clojure.xml/emit to turn the xml structure returned into text. As emit
   prints to *out* you may need to use with-out-str to capture the result.
   "
  (:use [clojure.contrib.zip-filter.xml :on [xml->]]
        (necessary-evil xml-utils value))
  (:require [clojure.zip :as zip]
            [clojure.contrib.zip-filter :as zf]
            [clojure.string :as su])
  (:import org.apache.commons.codec.binary.Base64))

(defrecord MethodCall [method-name parameters])

;; The following functions parse the xml structure of a <methodCall>
;; and return a new MethodCall record

(defn method-call?
  "a predicate to ensure that this xml is a methodCall"
  [x] (-> x zip/node :tag (= :methodCall)))

(defn parse-method-name
  "returns the method name for a methodcall"
  [x] (when-let [name (xml1-> x :methodName text)]
        (let [clean-name (.trim name)]
          (when (-> clean-name empty? not) (keyword clean-name)))))

(defn parse-params
  "returns a vector containing one element for each param in the method call"
  [x] (vec (map parse-value (xml-> x :params :param :value first-child))))

(defn parse
  "Takes xml structure representing an RPC method call and returns a new
   MethodCall record."
  [x] (when (method-call? x) 
        (MethodCall. (parse-method-name x) 
                     (parse-params x))))

;; The following functions are used to emit a <methodCall> xml structure

(defn unparse
  "This function returns a string that represents a method call record in the 
   XML format described by the xml-rpc 'spec'."
  [mc]
  (let [name-elem   (elem :methodName [(-> mc :method-name name)])
        params      (:parameters mc)
        children (if (seq params) 
                   [name-elem (elem :params (map #(elem :param
                                                        [(value-elem %)])
                                                 params))]
                   [name-elem])]
    (elem :methodCall children)))


