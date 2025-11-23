(ns main)

(require '[starfederation.datastar.clojure.api :as d*]
         '[starfederation.datastar.clojure.adapter.http-kit :as hk-gen])

(require '[hiccup2.core :as h])

;; Base response map
(def base-response
  {:status  200
   :headers {"Content-Type" "text/html"}})

;; Simple hello example
(def hello-page
  (->> [:html {:data-datastar-root true}
        [:head
         [:script
          {:type "module"
           :src  "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.6/bundles/datastar.js"}]]
        [:body
         [:div
          [:button {:data-on:click
                    ;; "@get('/say-hello')"
                    "@get('/chunked-hello')"
                    }
           "Press to say hello!"]
          [:p#hello-field]]]]
       h/html
       str
       (assoc base-response :body)))

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

;; Chunked hello example
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

;; connections
(def !connections (atom #{}))

(defn subscribe-handler [request]
  (hk-gen/->sse-response
   request
   {hk-gen/on-open (fn [sse-gen]
                     (swap! !connections conj sse-gen))
    hk-gen/on-close (fn [sse-gen status]
                      (swap! !connections disj sse-gen))}))

(defn broadcast-elements! [elements]
  (doseq [c @!connections]
    (d*/patch-elements! c elements)))

;; http-kit server
(require '[org.httpkit.server :as hk-server])

(defn app [{:keys [uri] :as req}]
  (case uri
    "/say-hello"     (simple-hello req)
    "/chunked-hello" (chunked-hello req)
    hello-page))

(def my-server
  (-> app
      (hk-server/run-server {:port 3000})))

(comment

  (my-server :timeout 100)

)
