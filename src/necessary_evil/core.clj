(ns necessary-evil.core
  (:require [necessary-evil.methodcall :as methodcall]))

(defn rpc-end-point
  "Returns a new ring handler that accepts a request and dispatches it to the
   appropriate method.

   methods-map is a map of keyword (method name) to fn"
  [methods-map]
  (fn [req] (if-let [method-call (methodcall/parse (:body req))]
             (if-let [method (methods-map (:method-name method-call))]
               (apply method (:parameters method-call))
               "fail")
             "fail")))


