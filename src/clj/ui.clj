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
    {:data-on:click "@get('/say-hello')"
     ;; :style         {:background-color "red"
     ;;                 :color            "white"
     ;;                 :padding          "10px 20px"
     ;;                 :text-align       "center"
     ;;                 :text-decoration  "none"
     ;;                 :display          "inline-block"
     ;;                 :font-size        "16px"
     ;;                 :margin           "4px 2px"
     ;;                 :cursor           "pointer"}
     }
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
           :href "/assets/css/base.css"}]]
        [:body
         (ui/hello-buttons)
         [:p#hello-field.mt-4.text-xl]]]
       h/html
       str))
