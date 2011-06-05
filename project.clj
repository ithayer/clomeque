(defproject clomeque "1.0.0-SNAPSHOT"
  :description "Clojure message queue"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [clj-time "0.3.0"]
                 [ring "0.3.8"]
		 [compojure "0.6.2"]
		 [aleph "0.2.0-alpha1"]
		 [lamina "0.4.0-alpha2-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]]
  :main clomeque.core)