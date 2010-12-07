(ns necessary-evil.methodcall
  "This module implements the methodCall wire format portion of the XML-RPC spec.
   You can find the official spect at http://www.xmlrpc.com/spec."
  (:use [clojure.contrib.zip-filter.xml :on [xml->]]
        (necessary-evil xml-utils value))
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.zip-filter :as zf]
            [clojure.string :as su])
  (:import org.apache.commons.codec.binary.Base64))

(defrecord MethodCall [method-name parameters])

;; The following functions parse the xml structure of a methodCall and return a
;; new MethodCall record

(defn method-call?
  "a predicate to ensure that this xml is a methodCall"
  [x] (-> x zip/node :tag (= :methodCall)))

(defn parse-method-name
  "returns the method name for a methodcall"
  [x] (when-let [name (xml1-> x :methodName text)]
        (let [clean-name (.trim name)]
          (when (-> clean-name empty? not) (keyword clean-name)))))

;; The following functions are used to emit a methodCall xml structure

(defn parse-params
  "returns a vector containing one element for each param in the method call"
  [x] (vec (map parse-value (xml-> x :params :param :value first-child))))

(defn parse-methodcall
  "Takes xml structure representing an RPC method call and returns a new MethodCall
   record."
  [x] (when (method-call? x) 
        (MethodCall. (parse-method-name x) 
                     (parse-params x))))

(defn emit-methodcall
  "This function returns a string that represents a method call record in the 
   XML format described by the xml-rpc 'spec'."
  [mc]
  (let [name-elem   (elem :methodName [(-> mc :method-name name)])
        params      (:parameters mc)
        children (if (seq params) 
                   [name-elem (elem :params (map #(elem :param [(value-elem %)])
                                                 params))]
                   [name-elem])]
    (with-out-str (xml/emit (elem :methodCall children)))))


