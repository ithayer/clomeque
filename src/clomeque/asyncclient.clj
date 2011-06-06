(ns clomeque.asyncclient
  (:require [clojure.contrib.logging :as lg])
  (:require [clojure.contrib.string  :as string])
  (:require [aleph.http.client       :as client])
  (:require [lamina.core             :as lamina])
  (:require [lamina.core.pipeline    :as pipeline])
  (:use aleph.formats)
  (:use clojure.pprint)
  (:use clojure.contrib.json)
  (:use ring.adapter.jetty)
  (:use ring.middleware.stacktrace)
  (:use compojure.core)
  (:gen-class))
  
(defn body-to-string [buf-seq]
  (apply str (map aleph.formats/byte-buffer->string buf-seq)))

(defn- make-request-with-callback [http-request callback]
  (pipeline/on-success
   (client/http-request http-request)
   (fn [r]
     (callback (body-to-string (:body r))))))
  

(defn create-queue [host queue callback]
  "Creates a queue named 'queue'."
  (make-request-with-callback {:method :put :url (str host "/queue/" queue)} callback))

(defn delete-queue [host queue callback]
  "Deletes a queue named 'queue'."
  (make-request-with-callback {:method :delete :url (str host "/queue/" queue)} callback))

(defn read-queue [host queue callback]
  "Reads from 'queue' and call 'callback' with the results."
  (make-request-with-callback {:method :get :url (str host "/queue/" queue)} callback))

(defn write-queue [host queue msg callback]
  "Writes to 'queue' and call 'callback' with the result."
  (make-request-with-callback {:method       :post
			       :url          (str host "/queue/" queue)
			       :content-type "application/json"
			       :body         msg} callback))

