(ns clomeque.asyncclient
  (:require [clojure.contrib.logging :as lg])
  (:require [clojure.contrib.string  :as string])
  (:require [aleph.http.client       :as client])
  (:require [lamina.core             :as lamina])
  (:require [lamina.core.pipeline    :as pipeline])
  (:use clojure.pprint)
  (:use clojure.contrib.json)
  (:use ring.adapter.jetty)
  (:use ring.middleware.stacktrace)
  (:use compojure.core)
  (:gen-class))
  
(defn read-channel [host channel callback]
  "Reads from 'channel'. Returns a 'result-channel' which you can call 'read-channel' on."
  (pipeline/on-success
   (client/http-request {:method :get :url (str host "/channels/" channel)})
   callback))

(defn update-channel [host channel msg callback]
  "Writes to 'channel'."
  (pipeline/on-success
   (client/http-request {:method       :post
			 :url          (str host "/channels/" channel)
			 :content-type "application/json"
			 :body         (json-str msg)})
   callback))
