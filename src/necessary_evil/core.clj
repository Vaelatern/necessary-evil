(ns necessary-evil.core
  "necessary-evil.core provides the main two functions required to act as a
   server and a client of xml-rpc.

   Use end-point to generate a new ring handler.
   Use call to call a method on a server." 
  (:require [clojure.string :as string]
            [clj-http.client :as http]
            [necessary-evil.methodcall :as methodcall]
            [necessary-evil.methodresponse :as methodresponse])
  (:use [necessary-evil.xml-utils :only [to-xml emit xml-from-stream]]
        [necessary-evil.fault :only [attempt-all fault]]))

;; server functions

(defn handle-post
  "handle-post processes an incoming post request for an xml-rpc end-point.
   this function also does all the error handling to check that the method exists,
   that methodcall is valid and catches any exception raised by the exposed function."
  [methods-map req]
  (let [result (try (attempt-all
                     [method-call (-> req :body xml-from-stream methodcall/parse
                                      (or (fault -2 "invalid methodcall")))
                      method-name (:method-name method-call)
                      method      (methods-map method-name
                                               (fault -1 (str "unknown method named "
                                                              (name method-name))))]
                     (apply method (:parameters method-call)))
                    (catch Exception e (fault -10 (str "Exception: " e))))]
    {:status 200
     :content-type "text/xml;charset=UTF-8"
     :body (-> result methodresponse/unparse emit with-out-str)}))

(defn end-point
  "Returns a new ring handler that accepts a request and dispatches it to the
   appropriate method.

   methods-map is a map of keyword (method name) to iFn."
  [methods-map]
  (let [methods-map (into {} (map (fn [[k v]] [(name k) v]) methods-map))]
    (bound-fn [req]
      (condp = (:request-method req)
          :post (handle-post methods-map req)
          :get {:status 200 ; get handler is merely a convenience for developers
                :body (string/join ", " (map name (keys methods-map)))}
          {:status 405 :body "Method Not Allowed"}))))

;; client functions

(defn ^{:added "2.0.0"}
  call*
  "Does a syncronous http request to the server and method provided.

   call* allows more control over how the call is sent to the endpoint server.
   Note that unlike call, call* takes a seq for args rather than as varags.

   Optional keyword arguments:
     post-fn - this function does the actual http request. Must take the same
       arguments as clj-http's post function. This is to allow you supply additional
       middleware etc that may be required for your particular needs.

     request - a map of all the request arguments as specified by clj-http. Any
       values provided here will override the defaults with the exception of :body
       which is (unsurprisingly) provided by call*

   See https://github.com/dakrone/clj-http/ for more details.
  "
  [endpoint-url method-name args & {:keys [post-fn request]
                                    :or   {post-fn http/post
                                           request {}}}]
  (io! "XML-RPC in transaction"
   (let [call (methodcall/methodcall method-name args)
         body (-> call methodcall/unparse emit with-out-str)
         post-params (merge {:content-type "text/xml;charset=UTF-8"}
                            request)
         response (post-fn endpoint-url (assoc post-params :body body))]
     (-> response
         :body
         to-xml
         methodresponse/parse))))


(defn call
  "Does a syncronous http request to the server and method provided.
   This is a simple interface. If you need more control over the http request,
   see call*"
  [endpoint-url method-name & args]
  (call* endpoint-url method-name args))
