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

;; a Queue actually consists of a lamina channel and out delay buffer
(defrecord Queue [channel delay-buffer])

;; a message, probably needs to become a protocol to support tostring
(defrecord Message [timestamp message])

;; queue constructor, simple
(defn create-queue []
  (Queue. (channel) []))

;; create a queue message
(defn create-message [msg]
  (Message. (System/currentTimeMillis) msg))

;; a ref to the queues in the system (do we need a ref?)
(def queues (ref {}))

;; TODO: content-types ... should be configurable?
(defn success-response [msg]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body msg})

;; TODO: error responses aren't always 404s !
(defn error-response [msg]
  {:status 404
   :headers {"content-type" "text/plain"}
   :body msg})

;; read a message from a queue (block if not there)
(defn read-queue [dest-queue res-channel req]
  (lg/info (str "Got read channel request" req))
  (receive (:channel dest-queue) 
	   #(enqueue res-channel {:status 200
				  :headers {"content-type" "text/plain"}
				  :body (str (:message %))})))

;; write a message to queue
(defn write-queue [dest-queue res-channel req]
  "Will write a message (obtained from the body of the request)
   to the provided destination queue"
  (let [body  (channel-buffer->string (input-stream->channel-buffer (:body req)))
	q-msg (create-message body)]
    (lg/info (str "Got body" body))
    ; enqueue a message into the destination queue's channel and delay buffer
    (enqueue (:channel dest-queue) q-msg)
    (conj (:delay-buffer dest-queue) body)
    ; send back a success response
    (enqueue res-channel (success-response "Message submitted!"))))

(defroutes web-routes
  (GET "/channels/:channel-name" [channel-name]
       (lg/info (str "Got read request for queue" channel-name))
       (let [q (get @queues channel-name)]
	 (if q
	   (wrap-aleph-handler (partial read-queue q))
	   (error-response (str "Channel" channel-name "does not exist")))))
  (PUT "/channels/:channel-name" [channel-name]
       (lg/info (str "Got request to create queue with name" channel-name))
       (dosync
	(if (@queues channel-name)
	  (error-response "Queue already exists")
	  (let [queues (alter queues assoc channel-name (create-queue))]
	    (success-response (str "Queue" channel-name "created!"))))))
  (POST "/channels/:channel-name" [channel-name]
	(lg/info (str "Got message submit request on queue" channel-name))
	(let [q (get @queues channel-name)]
	  (if q
	    (wrap-aleph-handler (partial write-queue q))
	    (error-response (str "Channel" channel-name "does not exist")))))
  (DELETE "/channels/:channel-name" [channel-name]
	  (lg/info (str "Got request to delete queue with name" channel-name))
	  (dosync
	   (if (@queues channel-name)
	     (do
	       (alter queues dissoc channel-name)
	       (success-response (str "Queue" channel-name "was deleted!")))
	     (error-response (str "Queue" channel-name "does not exist!")))))
  (route/not-found "Page not found"))

(defn -main [& args]
  (lg/info (str "Running with args: " (string/join " " args) 
		" :from: " (. System getProperty "user.dir")))
  (start-http-server (wrap-ring-handler web-routes) {:port 9502})
  (println "server started"))
