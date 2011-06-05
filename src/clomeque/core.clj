(ns clomeque.core
  (:require [clojure.contrib.logging :as lg])
  (:require [clojure.contrib.string  :as string])
  (:require [clj-time.core           :as t])
  (:require [clj-time.coerce         :as t-coerce])
  (:require [compojure.route         :as route]
	    [compojure.handler       :as handler])
  (:use lamina.core.channel)
  (:use aleph.http)
  (:use aleph.formats)
  (:require [clomeque.asyncclient    :as client])
  (:use clojure.pprint)
  (:use clojure.contrib.json)
  (:use ring.adapter.jetty)
  (:use ring.middleware.stacktrace)
  (:use compojure.core)
  (:gen-class))

(def broadcast-channel (channel 1))

(defn success-response []
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "success"})

(defn read-channel [res-channel req]
  (lg/info (str "Got read channel request" req))
  (receive broadcast-channel 
	   #(enqueue res-channel {:status 200
				  :headers {"content-type" "text/plain"}
				  :body (str %)})))

(defn write-to-channel [res-channel req]
  "Will write a message (obtained from the body of the request)
   to the single broadcast channel"
  (let [body (channel-buffer->string (input-stream->channel-buffer (:body req)))]
    (println "Got body" body)
    ; enqueue a message into the broad-cast channel
    (enqueue broadcast-channel body)
    ; send back a success response
    (enqueue res-channel (success-response))))

(defroutes web-routes
  (GET "/channels/:channel-name" [channel-name]
       (wrap-aleph-handler read-channel))
  (POST "/channels/:channel-name" [channel-name]
	(wrap-aleph-handler write-to-channel))
  (route/not-found "Page not found"))

(defn -main [& args]
  (lg/info (str "Running with args: " (string/join " " args) 
		" :from: " (. System getProperty "user.dir")))
  (start-http-server (wrap-ring-handler web-routes) {:port 9502})
  (println "server started"))
