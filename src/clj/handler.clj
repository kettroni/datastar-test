(ns handler
  (:require [clojure.data.json :as json]
            [hiccup2.core :as h]
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
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body ui/hello-page})

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
  (-> request
      (hk-gen/->sse-response
       {hk-gen/on-open (fn [sse-gen]
                         (doseq [sub @!subscribers]
                           (d*/patch-elements! sub (-> [:p#hello-field "new subscriber joined!"]
                                                       h/html
                                                       str)))
                         (swap! !subscribers conj sse-gen))
        hk-gen/on-close (fn [sse-gen status]
                          (swap! !subscribers disj sse-gen))})))

(defn broadcast-elements! [elements]
  (doseq [c @!subscribers]
    (d*/patch-elements! c elements)))

;; ── Chat ─────────────────────────────────────────────────────────────────────

(def !chat-subscribers (atom #{}))
(def !chat-messages (atom []))

(defn chat-page-handler [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body ui/chat-page})

(defn- render-message [{:keys [username text]}]
  (-> [:div.chat-message
       [:span.p-3 (str username ": ")]
       [:span.p-4 text]]
      h/html
      str))

(defn chat-subscribe-handler [request]
  (hk-gen/->sse-response
   request
   {hk-gen/on-open (fn [sse-gen]
                      ;; Mark the user as joined
                     (d*/patch-signals! sse-gen "{\"joined\": true}")
                      ;; Send existing messages to the new subscriber
                     (doseq [msg @!chat-messages]
                       (d*/patch-elements! sse-gen (render-message msg)
                                           {d*/selector "#chat-messages"
                                            d*/patch-mode d*/pm-append}))
                     (swap! !chat-subscribers conj sse-gen))
    hk-gen/on-close (fn [sse-gen _status]
                      (swap! !chat-subscribers disj sse-gen))}))

(defn chat-send-handler [request]
  (let [signals (-> request d*/get-signals slurp (json/read-str :key-fn keyword))
        username (:username signals)
        text (:message signals)]
    (when (and (seq username) (seq text))
      (let [msg {:username username :text text}
            html (render-message msg)]
        (swap! !chat-messages conj msg)
        (doseq [sub @!chat-subscribers]
          (d*/patch-elements! sub html
                              {d*/selector "#chat-messages"
                               d*/patch-mode d*/pm-append}))))
    {:status 200 :body ""}))
