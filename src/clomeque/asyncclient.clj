(ns clomeque.asyncclient
  (:require [clojure.contrib.logging :as lg])
  (:require [clojure.contrib.string  :as string])
  (:require [aleph.http.client       :as client])
  (:require [lamina.core             :as lamina])
  (:use clojure.pprint)
  (:use clojure.contrib.json)
  (:use ring.adapter.jetty)
  (:use ring.middleware.stacktrace)
  (:use compojure.core)
  (:gen-class))

(defn create [params]
  "Simply construct an asynchronous client. Params is like {:url 'http://localhost'}.
     Returns a function which accepts request hashes."
  (client/http-client params))

(defn- response-to-callback [result-channel callback]
  "Actually sends a request, and calls callback with response"
  (lamina/async 
   (loop [response ""]
     (when-not (lamina/drained? result-channel)
       (recur (apply str (concat response (lamina/read-channel result-channel)))))
     (callback response))))
  
(defn read-channel [client-fn channel callback]
  "Reads from 'channel'. Returns a 'result-channel' which you can call 'read-channel' on."
  (response-to-callback 
   (client-fn {:method :get, :url (str "/channels/" channel)})
   callback))

(defn update-channel [client-fn channel msg callback]
  "Writes to 'channel'."
  (response-to-callback
   (client-fn {:method       :post
	       :url          (str "/channels/" channel)
	       :content-type "application/json"
	       :body         (json-str msg)})
   callback))
