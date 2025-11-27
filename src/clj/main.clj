(ns main
  (:require [org.httpkit.server :as hk-server]
            [handler :as handler]
            [reitit.ring :as ring]))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get handler/base-handler}]

     ["/assets/*" (ring/create-file-handler)]

     ["/say-hello"
      {:get handler/simple-hello}]
     ["/chunked-hello"
      {:get handler/chunked-hello}]
     ["/subscribe"
      {:get handler/subscribe-handler}]
     ])))

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
