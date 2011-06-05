(ns clomeque.core
  (:require [clojure.contrib.logging :as lg])
  (:require [clojure.contrib.string  :as string])
  (:require [clj-time.core           :as t])
  (:require [clj-time.coerce         :as t-coerce])
  (:require [compojure.route         :as route]
	    [compojure.handler       :as handler])
  (:use lamina.core.channel)
  (:use aleph.http)
  (:use clojure.pprint)
  (:use clojure.contrib.json)
  (:use ring.adapter.jetty)
  (:use ring.middleware.stacktrace)
  (:use compojure.core)
  (:gen-class))

(def broadcast-channel (channel 1))

(defn read-channel [res-channel req]
  (receive broadcast-channel #(enqueue res-channel {:status 200
						    :headers {"content-type" "text/plain"}
						    :body %})))

(defroutes web-routes
  (GET "/channels/:channel-name" [channel-name]
       (wrap-aleph-handler read-channel))
  (route/not-found "Page not found"))

(defn -main [& args]
  (lg/info (str "Running with args: " (string/join " " args) 
		" :from: " (. System getProperty "user.dir")))
  (run-jetty (handler/site web-routes) {:port 9501}))
