(ns user
  (:require [main :as main]
            [org.httpkit.server :as hk-server]))

(defonce !server (atom nil))

(defn start! []
  (when-not @!server
    (reset! !server (hk-server/run-server main/app {:port 3000}))
    (println "Server started on http://localhost:3000"))
  :started)

(defn stop! []
  (when-let [server @!server]
    (server :timeout 100)
    (reset! !server nil)
    (println "Server stopped"))
  :stopped)

(defn restart! []
  (stop!)
  (start!))

;; Auto-start on REPL load
(start!)
