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

(defn- render-message [{:keys [username text time]}]
  (h> [:div {:class "flex items-baseline gap-2 px-2 py-1 border-b border-white/5 last:border-0"}
       [:span {:class "text-xs text-light/40 shrink-0 self-end"} time]
       [:span {:class "font-semibold text-accent shrink-0"} (str username ":")]
       [:span {:class "text-light break-words min-w-0"} text]]))

(defn chat-subscribe-handler [request]
  (hk-gen/->sse-response
   request
   {hk-gen/on-open (fn [sse-gen]
                     (d*/patch-elements! sse-gen
                                         (h> (ui/chat-area))
                                         {d*/selector "#username-section"
                                          d*/patch-mode d*/pm-outer})
                     (doseq [msg @!chat-messages]
                       (d*/patch-elements! sse-gen
                                           (render-message msg)
                                           {d*/selector "#chat-messages"
                                            d*/patch-mode d*/pm-append}))
                     (d*/execute-script! sse-gen "document.getElementById('message-input').focus()")
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
                 :text message
                 :time (-> (java.time.LocalTime/now)
                           (.format (java.time.format.DateTimeFormatter/ofPattern "HH:mm")))}
            html (render-message msg)]
        (swap! !chat-messages conj msg)
        (doseq [sub @!chat-subscribers]
          (d*/patch-elements! sub
                              html
                              {d*/selector "#chat-messages"
                               d*/patch-mode d*/pm-append}))))
    {:status 200 :body ""}))
