(ns ring-xml-rpc.parse
    (:use [clojure.contrib.zip-filter.xml])
    (:require [clojure.zip :as zip]
              [clojure.xml :as xml]
              [clojure.contrib.zip-filter :as zf]
              [clojure.contrib.str-utils2 :as su]
              [clj-time.core :as time]
              [clj-time.format :as time-format])
    (:import org.apache.commons.codec.binary.Base64))

; dave winer has kindly provided his own variation on ISO 8601 date time formatting
(def winer-time (time-format/formatter "yyyyMMdd'T'HH:mm:ss"))

;

(defrecord MethodCall [method-name arguments])

(defn method-call?
    "a predicate to ensure that this xml is a methodCall"
    [x] (-> x zip/node :tag (= :methodCall)))

(defn parse-method-name
    "returns the method name for a methodcall"
    [x] (when-let [name (xml1-> x :methodName text)]
            (when (-> name .trim empty? not) name)))

; value parsing
(declare parse-value)

(defn strip-leading-plus [s] (su/replace s #"^\s*\+" ""))

(defn parse-int [v] (Integer. (strip-leading-plus (text v))))

(defn parse-bool [v] (condp = (.trim (text v))
                        "0" false
                        "1" true
                        "throw exception here"))

(defn parse-struct [v] "barf")

(defn parse-array [v] (vec (map parse-value (xml-> v :array :data :value #(first (zf/children %))))))

(defmulti parse-value #(:tag (zip/node %)))

(defmethod parse-value :i4               [v] (parse-int v))
(defmethod parse-value :int              [v] (parse-int v))

(defmethod parse-value :boolean          [v] (parse-bool v))
(defmethod parse-value :string           [v] (text v))
(defmethod parse-value :double           [v] (Double. (strip-leading-plus (text v))))
(defmethod parse-value :dateTime.iso8601 [v] (time-format/parse winer-time (text v)))
(defmethod parse-value :base64           [v] (-> (text v) Base64/decodeBase64))
(defmethod parse-value :struct           [v] (parse-struct v))
(defmethod parse-value :array            [v] (parse-array v))
(defmethod parse-value :default          [v] (text v))

(defn parse-params
    "returns a vector containing one element for each param in the method call"
    [x] (vec (map parse-value (xml-> x :params :param :value #(first (zf/children %))))))

(defn parse-method-call
    "Takes xml structure representing an RPC method call"
    [x] (when (method-call? x) 
            (MethodCall. (parse-method-name x) 
                         (parse-params x))))