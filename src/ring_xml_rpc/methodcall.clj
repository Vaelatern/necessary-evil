(ns ring-xml-rpc.methodcall
    (:use [clojure.contrib.zip-filter.xml])
    (:require [clojure.zip :as zip]
              [clojure.xml :as xml]
              [clojure.contrib.zip-filter :as zf]
              [clojure.contrib.str-utils2 :as su]
              [clj-time.core :as time]
              [clj-time.format :as time-format])
    (:import org.apache.commons.codec.binary.Base64))

;; This module implements the methodCall format portion of XML-RPC spec.
;; You can find the official spec at http://www.xmlrpc.com/spec

;; dave winer has kindly defined his own variation on ISO 8601 date time formatting
(def winer-time (time-format/formatter "yyyyMMdd'T'HH:mm:ss"))

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

;; value parsing
(defn first-child [n] (first (zf/children n)))

(declare parse-value)

(defn strip-leading-plus [s] (su/replace s #"^\s*\+" ""))

(defn parse-int [v] (Integer. (strip-leading-plus (text v))))

(defn parse-bool [v] (condp = (.trim (text v))
                         "0" false
                         "1" true
                         "throw exception here"))

(defn parse-struct-member [m] (let [name (xml1-> m :name first-child)
                                    val  (xml1-> m :value first-child)]
                                (if (or name val)
                                  [(keyword (-> name text .trim)),
                                   (parse-value val)]
                                  [nil, nil])))

(defn parse-struct [v] (into {} (map parse-struct-member
                                     (xml-> v :struct :member))))

(defn parse-array [v] (vec (map parse-value
                                (xml-> v :array :data :value first-child))))

(defmulti parse-value #(:tag (zip/node %)))

(defmethod parse-value :i4               [v] (parse-int v))
(defmethod parse-value :int              [v] (parse-int v))

(defmethod parse-value :boolean          [v] (parse-bool v))
(defmethod parse-value :string           [v] (text v))
(defmethod parse-value :double           [v] (Double. (strip-leading-plus
                                                       (text v))))
(defmethod parse-value :dateTime.iso8601 [v] (time-format/parse winer-time
                                                                (text v)))
(defmethod parse-value :base64           [v] (-> (text v) Base64/decodeBase64))
(defmethod parse-value :struct           [v] (parse-struct v))
(defmethod parse-value :array            [v] (parse-array v))
(defmethod parse-value :default          [v] (text v))

(defn parse-params
  "returns a vector containing one element for each param in the method call"
  [x] (vec (map parse-value (xml-> x :params :param :value first-child))))

(defn parse-method-call
  "Takes xml structure representing an RPC method call and returns a new MethodCall
   record."
  [x] (when (method-call? x) 
        (MethodCall. (parse-method-name x) 
                     (parse-params x))))


;; The following functions are used to emit a methodCall xml structure
(defn- elem 
  "elem is a utility function to make creating xml/element structs (minus
   attributes)"
  ([name] (struct xml/element name))
  ([name children] (struct xml/element name nil children)))

(defprotocol ValueTypeElem
  "ValueTypeElem defines a single function 'value-type-elem' that is used to emit
   various types as values for the xml-rpc format"
  (value-type-elem [x]))

(defn value-elem
  "value-elem creates the <value> elements and the appropriate child element for it's
   arguments. This calls out to value-type-elem."
  [content] (elem :value [(value-type-elem content)]))

(defn- struct-member-elem
  "creates an xml element for a key-value pair."
  [[key value]] (elem :member [(elem :name [(name key)])
                               (value-elem value)]))

(extend-protocol ValueTypeElem
  String 
  (value-type-elem [this] (elem :string [this]))

  Integer
  (value-type-elem [this] (elem :int [(str this)]))

  Boolean
  (value-type-elem [this] (elem :boolean [(if this "1" "0")]))
  
  Double
  (value-type-elem [this] (elem :double [(str this)]))
                                       
  org.joda.time.DateTime
  (value-type-elem [this] (elem :dateTime.iso8601 [(time-format/unparse
                                                    winer-time
                                                    this)]))
  
  clojure.lang.IPersistentVector ; a vector becomes an xml-rpc array
  (value-type-elem [this] (elem :array [(elem :data (vec (map value-elem this)))]))
  
  clojure.lang.IPersistentMap ; a map becomes a xml-rpc struct 
  (value-type-elem [this] (elem :struct (vec (map struct-member-elem this)))))


(defn emit-method-call
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
