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
  
(defn body-to-string [buf]
  (lg/info "BEFORE")
  (let [ary (byte-array (.remaining buf))]
    (lg/info "HERE")
    (.get buf ary 0 (.remaining buf))
    (String. "SDFSDFSD")))

(defn read-queue [host queue callback]
  "Reads from 'queue' and call 'callback' with the results."
  (pipeline/on-success
   (client/http-request {:method :get :url (str host "/channels/" queue)})
   (fn [r]
     (callback (lg/spy (body-to-string (:body r)))))))

(defn write-queue [host queue msg callback]
  "Writes to 'queue' and call 'callback' with the result."
  (pipeline/on-success
   (client/http-request {:method       :post
			 :url          (str host "/channels/" queue)
			 :content-type "application/json"
			 :body         msg})
   (fn [r]
     (callback (byte-buffer->string (:body r))))))
