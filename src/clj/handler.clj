(ns handler
  (:require [hiccup2.core :as h]
            [starfederation.datastar.clojure.adapter.http-kit :as hk-gen]
            [starfederation.datastar.clojure.api :as d*]
            [ui]))

(def say-hello-content
  (-> [:p#hello-field "Hello world!"]
      h/html
      str))

(defn simple-hello [request]
  (let [response (hk-gen/->sse-response request
                                        {hk-gen/on-open
                                         (fn [sse-gen]
                                           (d*/patch-elements! sse-gen say-hello-content)
                                           (d*/close-sse! sse-gen))})]
    response))

(defn base-handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    ui/hello-page})

(def chunked1
  (-> [:p#hello-field "Hello"]
      h/html
      str))

(def chunked2
  (-> [:p#hello-field "Hello World!"]
      h/html
      str))

(defn chunked-hello [request]
  (hk-gen/->sse-response
   request
   {hk-gen/on-open (fn [sse-gen]
                     (d*/patch-elements! sse-gen chunked1)
                     (Thread/sleep 1000)
                     (d*/patch-elements! sse-gen chunked2)
                     (d*/close-sse! sse-gen))}))

(def subscriber-message
  (-> [:p#hello-field
       "hello subscriber!"]
      h/html
      str))

;; connections
(def !subscribers (atom #{}))

(defrecord Subscriber [name joined sse-gen])

(defn subscribe-handler [request]
  (hk-gen/->sse-response
   request
   {hk-gen/on-open (fn [sse-gen]
                     (doseq [sub @!subscribers]
                       (d*/patch-elements! sub (-> [:p#hello-field "new subscriber joined!"]
                                                   h/html
                                                   str)))
                     (swap! !subscribers conj sse-gen)
                     )
    hk-gen/on-close (fn [sse-gen status]
                      (swap! !subscribers disj sse-gen))}))

(defn broadcast-elements! [elements]
  (doseq [c @!subscribers]
    (d*/patch-elements! c elements)))
