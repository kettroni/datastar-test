(ns main
  (:require [org.httpkit.server :as hk-server]
            [ring.middleware.params :as params]
            [handler :as handler]
            [reitit.ring :as ring]))

(def app
  (-> (ring/ring-handler
       (ring/router [["/" {:get #(handler/base-handler %)}]
                     ["/examples" {:get #(handler/examples-page-handler %)}]
                     ["/assets/*" (ring/create-file-handler)]
                     ["/say-hello" {:get #(handler/simple-hello %)}]
                     ["/chunked-hello" {:get #(handler/chunked-hello %)}]
                     ["/subscribe" {:get #(handler/subscribe-handler %)}]
                     ["/chat" {:get #(handler/chat-page-handler %)}]
                     ["/chat/subscribe" {:get #(handler/chat-subscribe-handler %)}]
                     ["/chat/send" {:post #(handler/chat-send-handler %)}]]))
      params/wrap-params))

(defn -main [& _args]
  (let [port 3000]
    (hk-server/run-server app {:port port})
    (println (str "Server running on http://localhost:" port))
    @(promise)))

(comment

  (def my-server
    (-> app
        (hk-server/run-server {:port 3000})))

  (my-server :timeout 100)

  (handler/broadcast-elements! handler/subscriber-message)

  (require '[starfederation.datastar.clojure.api :as d*]
           '[hiccup2.core :as h])

  (->> @handler/!subscribers
       count)

  (->> @handler/!subscribers
       (map (partial d*/close-sse!))
       doall)

  (when-let [conn (first @handler/!subscribers)]
    (prn "Connections:" @handler/!subscribers)
    (prn "Attempting to patch first connection:")
    (d*/patch-elements! conn (-> [:p#hello-field "You are the first subscriber!"]
                                 h/html
                                 str))))
