(ns necessary-evil.core
  (:require [clojure.string :as string]
            [clj-http.client :as http]
            [necessary-evil.methodcall :as methodcall]
            [necessary-evil.methodresponse :as methodresponse])
  (:use [necessary-evil.xml-utils :only [to-xml emit xml-from-stream]])
  (:import necessary-evil.methodcall.MethodCall))


(defn handle-post
  [methods-map req]
  (let [result (try (if-let [method-call (-> req :body xml-from-stream methodcall/parse)]
                      (if-let [method (methods-map (:method-name method-call))]
                        (apply method (:parameters method-call))
                        (methodresponse/fault -1 (str "unknown method named "
                                                      (-> method-call
                                                          :method-name
                                                          name))))
                      (methodresponse/fault -2 "invalid methodcall"))
                    (catch Exception e (methodresponse/fault -10 (str "Exception: " e))))]
    {:status 200
     :body (-> result methodresponse/unparse emit with-out-str)}))

(defn end-point
  "Returns a new ring handler that accepts a request and dispatches it to the
   appropriate method.

   methods-map is a map of keyword (method name) to fn"
  [methods-map]
  (fn [req]
    (condp = (:request-method req)
        :post (handle-post methods-map req)
      :get {:status 200
            :body (string/join ", " (map name (keys methods-map)))})))


(defn call
  "Does a syncronous http request to the server and method provided."
  [endpoint-url method-name & args]
  (let [call (methodcall/MethodCall. method-name args)
        body (-> call methodcall/unparse emit with-out-str)]
    (-> (http/post endpoint-url {:body body}) :body to-xml methodresponse/parse)))
