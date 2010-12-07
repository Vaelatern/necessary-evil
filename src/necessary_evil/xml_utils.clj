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

(defn to-xml
  "to-xml takes a string and returns a new xml zipper"
  [xml-string] (-> xml-string
                   java.io.StringReader. 
                   org.xml.sax.InputSource. 
                   xml/parse
                   zip/xml-zip))

;; zip-filter selectors
(defn first-child [n] (first (zf/children n)))
