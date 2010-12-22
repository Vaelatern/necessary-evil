(ns necessary-evil.xml-utils
  "A collection of utilities for processing xml"
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.zip-filter :as zf]))

(defn elem 
  "elem is a utility function to make creating xml/element structs (minus
   attributes)"
  ([name] (struct xml/element name))
  ([name children] (struct xml/element name nil children)))

(defn xml-from-stream
  [stream]
  (-> stream xml/parse zip/xml-zip))

(defn to-xml
  "to-xml takes a string and returns a new xml zipper"
  [xml-string] (-> xml-string
                   java.io.StringReader. 
                   org.xml.sax.InputSource. 
                   xml-from-stream))

;; zip-filter selectors
(defn first-child [n] (first (zf/children n)))

;; the following have been copied from clojure.xml because the
;; python xmlrpc lib is real picky about white space and as it is a
;; canonical implementation its safe to assume this will effect real
;; code. As such these have been modified to not emit new lines

(defn emit-element [e]
  (if (instance? String e)
    (print e)
    (do
      (print (str "<" (name (:tag e))))
      (when (:attrs e)
	(doseq [attr (:attrs e)]
	  (print (str " " (name (key attr)) "='" (val attr)"'"))))
      (if (:content e)
	(do
	  (print ">")
	  (doseq [c (:content e)]
	    (emit-element c))
	  (print (str "</" (name (:tag e)) ">")))
	(print "/>")))))

(defn emit [x]
  (println "<?xml version='1.0' encoding='UTF-8'?>")
  (emit-element x))
