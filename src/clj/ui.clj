(ns ui
  (:require [clojure.string :as s]
            [hiccup2.core :as h]))

(def datastar-cdn
  "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.6/bundles/datastar.js")

(defn ->kw [styles]
  (->> styles
       (map name)
       (s/join ".")
       keyword))

(defn hello-buttons []
  [:div
   [:div
    {:data-on:click "@get('/say-hello')"}
    "Press to hello!"]
   [:div
    {:data-on:click "@get('/chunked-hello')"}
    "Press to chuncked hello!"]
   [:div
    {:data-on:click "@get('/subscribe')"}
    "Press to subscribe hello channel!"]])

(def hello-page
  (->> [:html
        {:data-datastar-root true}
        [:head
         [:script
          {:type "module"
           :src  ui/datastar-cdn}]
         [:link
          {:rel  "stylesheet"
           :href "/assets/css/colors.css"}]
         [:link
          {:rel  "stylesheet"
           :href "/assets/css/layout.css"}]]
        [:body
         (ui/hello-buttons)
         [:p#hello-field.mt-4.text-xl]]]
       h/html
       str))
