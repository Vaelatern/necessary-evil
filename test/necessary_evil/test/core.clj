(ns necessary-evil.test.core
  (:use [necessary-evil.core] :reload)
  (:require [necessary-evil.methodcall :as methodcall]
            [necessary-evil.methodresponse :as methodresponse]
            :reload)
  (:use [necessary-evil.value] :reload)
  (:use [clojure.test]
        [necessary-evil.xml-utils :only [to-xml emit]])
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:require [clj-time.core :as time]
            [clojure.xml :as xml])
  (:import org.apache.commons.codec.binary.Base64))


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

(defxml method-call-empty-default-arg xml-prolog
        "<methodCall>
            <methodName>test.method.name</methodName>
            <params><param><value></value></param></params>
        </methodCall>")

(defxml github-issue-4-xml xml-prolog
  "<methodResponse><params><param><value><struct><member><name>foo</name><value></value></member><member><name>bar</name><value>xyz</value></member></struct></value></param></params></methodResponse>")

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
    (is (= (methodcall/method-call? not-method-call) false) "methld-call? should not be true for <methodCall> rooted documents")
    (is (methodcall/method-call? empty-method-call))
    (is (methodcall/method-call? method-call-empty-method-name))
    (is (methodcall/method-call? method-call-test-method-name)))


(deftest method-names
  (is (= (methodcall/parse-method-name empty-method-call) nil) 
      "methodName should be nil if missing")
  (is (= (methodcall/parse-method-name method-call-empty-method-name) nil)
       "methodname should be nil if empty")
  (is (= (methodcall/parse-method-name method-call-test-method-name) "test.method.name")))

(deftest params
  (is (= (methodcall/parse-params method-call-default-arg) ["string"]) 
      "did not parse untyped string correctly")
  (is (= (methodcall/parse-params method-call-empty-default-arg) [""])
      "did not parse untyped empty string correctly")
  (is (= (methodcall/parse-params method-call-string-arg) ["string"]) 
      "did not parse string correctly")
  (is (= (methodcall/parse-params method-call-int-arg)
         [1 0 -1 1000 -1000 42 1 0 -1 1000 -1000 42]) 
      "did not parse ints correctly")
  (is (java.util.Arrays/equals (first (methodcall/parse-params method-call-base64-arg)) base64text-plain) 
      "did not parse base64 correctly") 
  (is (= (methodcall/parse-params method-call-double-arg) [1.0 0.0 -1.0 1000.0 -1000.0 42.0 1.0 0.0 -1.0 1000.0 -1000.0 42.0 1.5 0.0 -1.5 1000.50 -1000.50 42.234]) 
      "did not parse doubles correctly")
  (is (= (methodcall/parse-params method-call-bool-arg) [true false ]) 
      "did not parse bools correctly")
  (is (= (first (methodcall/parse-params method-call-date-arg)) (time/date-time 2010 11 29 21 02 34)) 
      "did not parse date correctly"))
      
(deftest compound-params
  (is (= (methodcall/parse-params method-call-array-arg) [[] ["string"] ["string" 123 [true]]])
      "did not parse arrays correctly")
  (is (= (methodcall/parse-params method-call-struct-arg) [{} {:key "value"} {:numeral 123, :composite {:key "value", :array! ["frobitz"]}}])
      "did not parse arrays correctly"))
  
(deftest value-type-elem-test
  (is (= (value-type-elem "hello") {:tag :string :attrs nil :content ["hello"]})
      "string generates correct nodes")
  (is (= (value-type-elem 1) {:tag :int :attrs nil :content ["1"]}) 
      "int generates correct nodes")
  (is (= (value-type-elem true) {:tag :boolean :attrs nil :content ["1"]}) 
      "true generates correct nodes")
  (is (= (value-type-elem false) {:tag :boolean :attrs nil :content ["0"]}) 
      "true generates correct nodes")
  (is (= (value-type-elem 1.2) {:tag :double :attrs nil :content ["1.2"]})
      "double generates correct nodes")
  (is (= (value-type-elem []) 
        {:tag :array :attrs nil :content 
            [{:tag :data :attrs nil :content []}]})
      "empty array generates correct nodes")
  (is (= (value-type-elem [1]) 
        {:tag :array :attrs nil :content 
          [{:tag :data :attrs nil :content 
            [{:tag :value :attrs nil :content 
              [{:tag :int :attrs nil :content ["1"]}]}]}]})
      "array of 1 item generates correct nodes"))
      
(deftest emit-method-call-test
    (is (= (to-xml (with-out-str (emit (methodcall/unparse (necessary-evil.methodcall.MethodCall. :test.method.name [])))))
         method-call-test-method-name)))


(defn- ping-server [port]
 (let [ep (end-point {"method" (fn [argument] {"argument" argument})})]
   (run-jetty ep {:port port :join? false})))

(deftest chars-rountrip
 (let [port 12254
       jetty-server (ping-server port)
       non-US-ASCII "Eléonore"]
   (try
     ;; test non-US-ASCII chars roundtrip
     (is (= non-US-ASCII
            (:argument (call (str "http://localhost:" port)
                             "method"
                             non-US-ASCII))))
     (finally (.stop jetty-server)))))


(deftest github-issue-4
  (is (methodresponse/parse github-issue-4-xml)
      {:foo ""
       :bar "xyz"}))

