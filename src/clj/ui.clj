(ns ui
  (:require [hiccup2.core :as h]))

(defmacro h>
  "Renders hiccup form(s) to an HTML string."
  [& forms]
  `(str (h/html ~@forms)))

(def datastar-cdn
  "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.6/bundles/datastar.js")

(defn hello-buttons []
  [:div.button-group
   [:button.bg-2.p-3
    {:data-on:click "@get('/say-hello')"}
    "Press to hello!"]
   [:button.bg-3.p-4
    {:data-on:click "@get('/chunked-hello')"}
    "Press to chuncked hello!"]
   [:button.bg-4.p-1
    {:data-on:click "@get('/subscribe')"}
    "Press to subscribe hello channel!"]
   [:a {:href "/chat"}
    [:button.bg-3.p-3 "Join Chat"]]])

(def home-page
  (h> [:html
       {:data-datastar-root true}
       [:head
        [:script {:type "module"
                  :src ui/datastar-cdn}]
        [:link {:rel "stylesheet"
                :href "/assets/css/colors.css"}]
        [:link {:rel "stylesheet"
                :href "/assets/css/layout.css"}]]
       [:body.bg-1.p-5
        [:h1.p-4.text-2xl "Welcome"]
        [:div.button-group
         [:a {:href "/chat"}
          [:button.bg-2.p-3 "Join Chat"]]
         [:a {:href "/examples"}
          [:button.bg-3.p-3 "Examples"]]]]]))

(def examples-page
  (h> [:html
       {:data-datastar-root true}
       [:head
        [:script {:type "module"
                  :src ui/datastar-cdn}]
        [:link {:rel "stylesheet"
                :href "/assets/css/colors.css"}]
        [:link {:rel "stylesheet"
                :href "/assets/css/layout.css"}]]
       [:body.bg-1.p-5
        (ui/hello-buttons)
        [:p#hello-field.mt-4.text-xl]]]))

(def chat-page
  (h> [:html
       {:data-datastar-root true}
       [:head
        [:script {:type "module" :src ui/datastar-cdn}]
        [:link {:rel "stylesheet" :href "/assets/css/colors.css"}]
        [:link {:rel "stylesheet" :href "/assets/css/layout.css"}]]
       [:body.bg-1.p-5
        {:data-signals "{\"username\": \"\", \"message\": \"\", \"joined\": false}"}
        [:h1.p-4.text-2xl "Chat"]

       ;; Username input — shown until user has joined
        [:div#username-section
         {:data-show "!$joined"}
         [:input.p-1
          {:type "text"
           :placeholder "Enter your username..."
           :data-bind "username"}]
         [:button.bg-2.p-3
          {:data-on:click "@get('/chat/subscribe')"}
          "Join"]]
        [:div#chat-area
         {:data-show "$joined"}
         [:div#chat-messages.mt-4]
         [:div.mt-4
          [:input.p-1
           {:type "text"
            :placeholder "Type a message..."
            :data-bind "message"}]
          [:button.bg-2.p-3
           {:data-on:click "@post('/chat/send')"}
           "Send"]]]]]))
