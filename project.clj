(defproject necessary-evil "1.1.0"
  :description "An implementation of XML-RPC for the Clojure Ring HTTP stack"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring/ring-core "0.3.5"]
                 [clj-http "0.1.3"]
                 [commons-codec "1.4"]
                 [clj-time "0.3.0-SNAPSHOT"]]
  :dev-dependencies [[ring/ring-jetty-adapter "0.3.5"]
                     [marginalia "0.5.0-alpha"]])
