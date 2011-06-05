(ns clomeque.core
  (:require [clojure.contrib.logging :as lg])
  (:require [clojure.contrib.string  :as string])
  (:require [clj-time.core           :as t])
  (:require [clj-time.coerce         :as t-coerce])
  (:require [compojure.route         :as route]
	    [compojure.handler       :as handler])
  (:use clojure.pprint)
  (:use clojure.contrib.json)
  (:use ring.adapter.jetty)
  (:use ring.middleware.stacktrace)
  (:use compojure.core)
  (:gen-class))

(defroutes web-routes
  (GET "/" [] { :body "Hello World"})
  (route/not-found "Page not found"))

(defn -main [& args]
  (lg/info (str "Running with args: " (string/join " " args) 
		" :from: " (. System getProperty "user.dir")))
  (run-jetty (handler/site web-routes) {:port 9501}))

