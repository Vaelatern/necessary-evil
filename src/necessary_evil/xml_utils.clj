(ns necessary-evil.xml-utils
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.contrib.zip-filter :as zf]))

;; a collection of utilities for munging xml

(defn elem 
  "elem is a utility function to make creating xml/element structs (minus
   attributes)"
  ([name] (struct xml/element name))
  ([name children] (struct xml/element name nil children)))

(defn first-child [n] (first (zf/children n)))
