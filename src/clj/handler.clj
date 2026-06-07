(ns handler
  (:require [clojure.data.json :as json]
            [starfederation.datastar.clojure.adapter.http-kit :as hk-gen]
            [starfederation.datastar.clojure.api :as d*]
            [ui :refer [h>]]))

(def say-hello-content
  (h> [:p#hello-field "Hello world!"]))

(defn simple-hello [request]
  (let [response (hk-gen/->sse-response request
                                        {hk-gen/on-open
                                         (fn [sse-gen]
                                           (d*/patch-elements! sse-gen say-hello-content)
                                           (d*/close-sse! sse-gen))})]
    response))

(defn base-handler [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body ui/home-page})

(defn examples-page-handler [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body ui/examples-page})

(def chunked1
  (h> [:p#hello-field "Hello"]))

(def chunked2
  (h> [:p#hello-field "Hello World!"]))

(defn chunked-hello [request]
  (hk-gen/->sse-response
   request
   {hk-gen/on-open (fn [sse-gen]
                     (d*/patch-elements! sse-gen chunked1)
                     (Thread/sleep 1000)
                     (d*/patch-elements! sse-gen chunked2)
                     (d*/close-sse! sse-gen))}))

(def subscriber-message
  (h> [:p#hello-field "hello subscriber!"]))

;; connections
(def !subscribers (atom #{}))

(defrecord Subscriber [name joined sse-gen])

(defn subscribe-handler [request]
  (-> request
      (hk-gen/->sse-response
       {hk-gen/on-open (fn [sse-gen]
                         (doseq [sub @!subscribers]
                           (d*/patch-elements! sub (h> [:p#hello-field "new subscriber joined!"])))
                         (swap! !subscribers conj sse-gen))
        hk-gen/on-close (fn [sse-gen status]
                          (swap! !subscribers disj sse-gen))})))

(defn broadcast-elements! [elements]
  (doseq [c @!subscribers]
    (d*/patch-elements! c elements)))

(def !chat-subscribers (atom #{}))
(def !chat-messages (atom []))

(defn chat-page-handler [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body ui/chat-page})

(defn- render-message [{:keys [username text]}]
  (let [time-str (-> (java.time.LocalTime/now)
                     (.format (java.time.format.DateTimeFormatter/ofPattern "HH:mm")))]
    (h> [:div.chat-message
         [:span.message-time time-str]
         [:span.username.color-2 (str username ":")]
         [:span.text.color-4 text]])))

(defn chat-subscribe-handler [request]
  (hk-gen/->sse-response
   request
   {hk-gen/on-open (fn [sse-gen]
                     (d*/patch-signals! sse-gen "{\"joined\": true}")
                     (doseq [msg @!chat-messages]
                       (d*/patch-elements! sse-gen
                                           (render-message msg)
                                           {d*/selector "#chat-messages"
                                            d*/patch-mode d*/pm-append}))
                     (swap! !chat-subscribers conj sse-gen))
    hk-gen/on-close (fn [sse-gen _status]
                      (swap! !chat-subscribers disj sse-gen))}))

(defn chat-send-handler [request]
  (let [{:keys [username
                message]} (-> request
                              d*/get-signals
                              slurp
                              (json/read-str :key-fn keyword))]
    (when (and (seq username)
               (seq message))
      (let [msg {:username username
                 :text message}
            html (render-message msg)]
        (swap! !chat-messages conj msg)
        (doseq [sub @!chat-subscribers]
          (d*/patch-elements! sub
                              html
                              {d*/selector "#chat-messages"
                               d*/patch-mode d*/pm-append}))))
    {:status 200 :body ""}))
