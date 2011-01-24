(ns necessary-evil.errorm
  "errorm provides a monad to make handling errors easier. It also provides a form called attempt-all
   to simplify using it."
  (:use [clojure.contrib.monads :only [defmonad domonad]]))

(defrecord Failure [message])

(defn fail [message] (Failure. message))

(defprotocol ComputationFailed
  "A protocol that determines if a computation has resulted in a failure.
   This allows the definition of what constitutes a failure to be extended
   to new types by the consumer."
  (has-failed? [self]))

(extend-protocol ComputationFailed
  Object
  (has-failed? [self] false)
  
  Failure
  (has-failed? [self] true)
  
  Exception 
  (has-failed? [self] true))

(defmonad error-m 
  [m-result identity
   m-bind   (fn [m f] (if (has-failed? m)
                       m
                       (f m)))])

(defmacro attempt-all
  "attempt-all is an alternative to complex nested if-let and when-let forms."
  ([bindings return] `(domonad error-m ~bindings ~return))
  ([bindings return else]
     `(let [result# (attempt-all ~bindings ~return)]
        (if (has-failed? result#) ~else
            result#))))


