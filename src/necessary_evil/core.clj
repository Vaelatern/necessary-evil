(ns necessary-evil.core
  (:require [clj-http.client :as http]
            [necessary-evil.methodcall :as methodcall]
            [necessary-evil.methodresponse :as methodresponse])
  (:use [necessary-evil.xml-utils :only [to-xml emit]])
  (:import necessary-evil.methodcall.MethodCall))

(defn rpc-end-point
  "Returns a new ring handler that accepts a request and dispatches it to the
   appropriate method.

   methods-map is a map of keyword (method name) to fn"
  [methods-map]
  (fn [req] (if-let [method-call (methodcall/parse (:body req))]
             (if-let [method (methods-map (:method-name method-call))]
               (apply method (:parameters method-call))
               (methodresponse/fault -1 (str "unknown method named "
                                             (-> method-call
                                                 :method-name
                                                 name))))
             "fail")))


(defn rpc-call
  "Does a syncronous http request to the server and method provided."
  [endpoint-url method-name & args]
  (let [call (methodcall/MethodCall. method-name args)
        body (-> call methodcall/unparse emit with-out-str)]
    (-> (http/post endpoint-url {:body body}) :body to-xml methodresponse/parse)))
