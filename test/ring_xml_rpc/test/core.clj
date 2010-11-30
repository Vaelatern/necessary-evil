(ns ring-xml-rpc.test.core
  (:use [ring-xml-rpc.core] :reload)
  (:use [ring-xml-rpc.methodcall] :reload)
  (:use [clojure.test])
  (:require [clj-time.core :as time])
  (:import org.apache.commons.codec.binary.Base64))

(defn to-xml [s] (-> s java.io.StringReader. 
                       org.xml.sax.InputSource. 
                       clojure.xml/parse
                       clojure.zip/xml-zip))

(defmacro defxml 
    "Creates an xml blob from some strings"
    [name & body] `(def ~name (-> (str ~@body) to-xml)))

; the following define some XML strings for us
(def xml-prolog "<?xml version=\"1.0\"?>")

(defxml not-method-call xml-prolog "<foo></foo>")
(defxml empty-method-call xml-prolog "<methodCall></methodCall>")

(defxml method-call-empty-method-name xml-prolog
    "<methodCall>
        <methodName></methodName>
    </methodCall>")

(defxml method-call-test-method-name xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
        </methodCall>")

(defxml method-call-default-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params><param><value>string</value></param></params>
        </methodCall>")

(defxml method-call-string-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params><param><value><string>string</string></value></param></params>
        </methodCall>")

(defxml method-call-int-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params>
                <param><value><i4>1</i4></value></param>
                <param><value><i4>0</i4></value></param>
                <param><value><i4>-1</i4></value></param>
                <param><value><i4>1000</i4></value></param>
                <param><value><i4>-1000</i4></value></param>
                <param><value><i4>+42</i4></value></param>
                <param><value><int>1</int></value></param>
                <param><value><int>0</int></value></param>
                <param><value><int>-1</int></value></param>
                <param><value><int>1000</int></value></param>
                <param><value><int>-1000</int></value></param>
                <param><value><int>+42</int></value></param>
            </params>
        </methodCall>")

(defxml method-call-double-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params>
                <param><value><double>1</double></value></param>
                <param><value><double>0</double></value></param>
                <param><value><double>-1</double></value></param>
                <param><value><double>1000</double></value></param>
                <param><value><double>-1000</double></value></param>
                <param><value><double>+42</double></value></param>
                <param><value><double>1.0</double></value></param>
                <param><value><double>0.0</double></value></param>
                <param><value><double>-1.0</double></value></param>
                <param><value><double>1000.0</double></value></param>
                <param><value><double>-1000.0</double></value></param>
                <param><value><double>+42.0</double></value></param>
                <param><value><double>1.5</double></value></param>
                <param><value><double>0.0</double></value></param>
                <param><value><double>-1.5</double></value></param>
                <param><value><double>1000.50</double></value></param>
                <param><value><double>-1000.50</double></value></param>
                <param><value><double>+42.234</double></value></param>
            </params>
        </methodCall>")
        
(defxml method-call-bool-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params>
                <param><value><boolean>1</boolean></value></param>
                <param><value><boolean>0</boolean></value></param>
            </params>
        </methodCall>")
        
(def base64text-plain (.getBytes "hello, world"))
(def base64text-encoded (Base64/encodeBase64 base64text-plain))

(defxml method-call-base64-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params><param><value><base64>"
            (String. base64text-encoded)
            "</base64></value></param></params>
        </methodCall>")

(defxml method-call-date-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params>
                <param><value><dateTime.iso8601>20101129T21:02:34</dateTime.iso8601></value></param>
            </params>
        </methodCall>")

(defxml method-call-array-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params>
                <param>
                    <value>
                        <array>
                            <data>
                            </data>
                        </array>
                    </value>
                </param>
                <param>
                    <value>
                        <array>
                            <data>
                                <value>string</value>
                            </data>
                        </array>
                    </value>
                </param>
                <param>
                    <value>
                        <array>
                            <data>
                                <value>string</value>
                                <value><i4>123</i4></value>
                                <value>
                                    <array>
                                        <data>
                                            <value>
                                                <boolean>1</boolean>
                                            </value>
                                        </data>
                                    </array>
                                </value>
                            </data>
                        </array>
                    </value>
                </param>
            </params>
        </methodCall>")

(defxml method-call-struct-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params>
                <param>
                    <value>
                        <struct>
                        </struct>
                    </value>
                </param>
                <param>
                    <value>
                        <struct>
                            <member>
                                <name>key</name>
                                <value><string>value</string></value>
                            </member>
                        </struct>
                    </value>
                </param>
                <param>
                    <value>
                        <struct>
                            <member>
                                <name>numeral</name>
                                <value><i4>123</i4></value>
                            </member>
                            <member>
                                <name>composite</name>
                                <value>
                                    <struct>
                                        <member>
                                            <name>key</name>
                                            <value><string>value</string></value>
                                        </member>
                                        <member>
                                            <name>array!</name>
                                            <value>
                                                <array>
                                                    <data>
                                                        <value>frobitz</value>
                                                    </data>
                                                </array>
                                            </value>
                                        </member>
                                    </struct>
                                </value>
                            </member>
                        </struct>
                    </value>
                </param>
            </params>
        </methodCall>")


; Actual tests to follow

(deftest is-method-call
    (is (= (method-call? not-method-call) false) "methld-call? should not be true for <methodCall> rooted documents")
    (is (method-call? empty-method-call))
    (is (method-call? method-call-empty-method-name))
    (is (method-call? method-call-test-method-name)))


(deftest method-names
  (is (= (parse-method-name empty-method-call) nil) 
      "methodName should be nil if missing")
  (is (= (parse-method-name method-call-empty-method-name) nil)
       "methodname should be nil if empty")
  (is (= (parse-method-name method-call-test-method-name) :test.method.name)))

(deftest params
  (is (= (parse-params method-call-default-arg) ["string"]) 
      "did not parse untyped string correctly")
  (is (= (parse-params method-call-string-arg) ["string"]) 
      "did not parse string correctly")
  (is (= (parse-params method-call-int-arg) [1 0 -1 1000 -1000 42 1 0 -1 1000 -1000 42]) 
      "did not parse ints correctly")
  (is (java.util.Arrays/equals (first (parse-params method-call-base64-arg)) base64text-plain) 
      "did not parse base64 correctly") 
  (is (= (parse-params method-call-double-arg) [1.0 0.0 -1.0 1000.0 -1000.0 42.0 1.0 0.0 -1.0 1000.0 -1000.0 42.0 1.5 0.0 -1.5 1000.50 -1000.50 42.234]) 
      "did not parse doubles correctly")
  (is (= (parse-params method-call-bool-arg) [true false ]) 
      "did not parse bools correctly")
  (is (= (first (parse-params method-call-date-arg)) (time/date-time 2010 11 29 21 02 34)) 
      "did not parse date correctly"))
      
(deftest compound-params
  (is (= (parse-params method-call-array-arg) [[] ["string"] ["string" 123 [true]]])
      "did not parse arrays correctly")
  (is (= (parse-params method-call-struct-arg) [{} {:key "value"} {:numeral 123, :composite {:key "value", :array! ["frobitz"]}}])
      "did not parse arrays correctly"))
  
  