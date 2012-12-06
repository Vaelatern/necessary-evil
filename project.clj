(defproject necessary-evil "2.0.0"
  :description "An implementation of XML-RPC for the Clojure Ring HTTP stack"

  :url "https://github.com/brehaut/necessary-evil/"
  :scm {:url "git://github.com/brehaut/necessary-evil.git"}
  :license {:name "Eclipse Public License",
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :dependencies {org.clojure/clojure "1.4.0",
                 org.clojure/data.zip "0.1.0",
                 org.clojure/algo.monads "0.1.3-20120910.070123-6", ; lock to a specific snapshot
                 clj-http "0.5.8",
                 commons-codec "1.4",
                 clj-time "0.4.4",
                 commons-lang "2.6"}
  :profiles {:dev {:dependencies {ring/ring-jetty-adapter "1.0.1", marginalia "0.7.0-SNAPSHOT"}
                   :plugins {lein-marginalia "0.7.0-SNAPSHOT"}}}
  :repositories {"sonatype-snapshots"
                 "https://oss.sonatype.org/content/repositories/snapshots/"}
  :min-lein-version "2.0.0"

  :pom-addition [:developers [:developer
                              [:name "Andrew Brehaut"]
                              [:url "http://brehaut.net"]
                              [:email "andrew@brehaut.net"]
                              [:timezone "+12"]]])