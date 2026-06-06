(ns dev
  (:require [clojure.string :as str]
            [babashka.process :as p :refer [shell]]))

(defn check-status
  "Runs a curl command and returns the HTTP status code string."
  [& curl-args]
  (let [res (apply shell {:out :string :err :string :continue true} curl-args)]
    (str/trim (:out res))))

(defn smoke-test []
  (println "=== GET /chat ===")
  (let [code (check-status "curl -sf -o /dev/null -w \"%{http_code}\" http://localhost:3000/chat")]
    (println (if (= "200" code) "✓ 200 OK" (str "✗ " code))))

  (println "=== POST /chat/send ===")
  (let [code (check-status "curl -s -o /dev/null -w \"%{http_code}\" -X POST http://localhost:3000/chat/send -H \"Content-Type: application/json\" -d '{\"username\":\"bb\",\"message\":\"smoke test\"}'")]
    (println (if (= "200" code) "✓ 200 OK" (str "✗ " code))))

  (println "Done."))

(defn ensure-chrome []
  (if (= 0 (:exit (shell {:out :string :err :string :continue true}
                         "curl -sf http://localhost:9222/json/version")))
    (println "✓ Headless Chromium already running on port 9222")
    (do
      (println "Starting headless Chromium on port 9222...")
      (p/process {:out :inherit :err :inherit}
                 "chromium" "--headless=new" "--remote-debugging-port=9222"
                 "--no-first-run" "--no-default-browser-check"
                 "--disable-extensions" "--disable-background-networking"
                 "--disable-sync" "--no-sandbox" "about:blank")
      (Thread/sleep 1500)
      (println "✓ Chromium started"))))

(defn restart-chrome []
  (println "Stopping Chromium on port 9222...")
  (shell {:continue true} "pkill -f 'chromium.*remote-debugging-port=9222'")
  (Thread/sleep 800)
  (ensure-chrome))
