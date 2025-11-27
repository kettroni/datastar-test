(ns main
  (:require [org.httpkit.server :as hk-server]
            [handler :as handler]))

(defn app [{:keys [uri] :as req}]
  (case uri
    "/say-hello"     (handler/simple-hello req)
    "/chunked-hello" (handler/chunked-hello req)
    "/subscribe"     (handler/subscribe-handler req)
    (handler/base-handler req)))

(comment

  (def my-server
    (-> app
        (hk-server/run-server {:port 3000})))

  (my-server :timeout 100)

  (handler/broadcast-elements! handler/subscriber-message)

  (->> @handler/!subscribers
       (map (partial handler/close-sse!))
       doall)

  (require '[starfederation.datastar.clojure.api :as d*]
            '[hiccup2.core :as h])

  (when-let [conn (first @handler/!subscribers)]
     (prn "Connections:" @handler/!subscribers)
     (prn "Attempting to patch first connection:")
     (d*/patch-elements! conn (-> [:p#hello-field "You are the first subscriber!"]
                                  h/html
                                  str)))

  )
