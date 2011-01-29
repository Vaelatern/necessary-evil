(ns necessary-evil.fault
  "Provides the fault record type required to support XML-RPC fault returns.

   Functions and forms to make working with faults easier are also provided.
   attempt-all will short circuit if any Fault is encountered."
  (:use [clojure.contrib.monads :only [monad domonad]]))

;; returning a Fault record from any piece of rpc handler will result
;; in a fault methodResponse being generated. Anything else will
;; generate a params blob

(defrecord Fault [^Integer fault-code ^String fault-string])

(defn fault
  "Convenience function that creates a new fault record.

   fault-code must be an integer. The exact meaning of this value is not
   prescribed by the spec. By convention necessary-evil assigns fault-codes
   below 0 to indicate rpc level fault, and assumes users will use 1 and
   greater to indicate faults for the specific handlers.

   fault-string is a string that contains a human readable description of
   the fault in question. Again the spec has nothing to say about the content
   of this field."
  [fault-code fault-string]
  (Fault. fault-code fault-string))

(def fault? (partial instance? Fault))

;; Utilities for handling faults:

(defn new-error-m
  "Creates a new monad that short circuits if any part of the computation
   fails. For instance, passing `nil?' produces a maybe-m."
  [has-failed?]
  (monad
   [m-result identity
    m-bind   (fn [m f] (if (has-failed? m)
                        m
                        (f m)))]))

(def error-on-fault-m (new-error-m fault?))

(defmacro attempt-all
  "attempt-all is an alternative to complex nested if-let and when-let forms."
  ([bindings return] `(domonad error-on-fault-m ~bindings ~return))
  ([bindings return else]
     `(let [result# (attempt-all ~bindings ~return)]
        (if (fault? result#) ~else
            result#))))


