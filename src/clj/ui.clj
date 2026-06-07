(ns ui
  (:require [hiccup2.core :as h]))

(defmacro h>
  "Renders hiccup form(s) to an HTML string."
  [& forms]
  `(str (h/html ~@forms)))

(def datastar-cdn
  "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.6/bundles/datastar.js")

(def tailwind-cdn
  "https://cdn.tailwindcss.com")

(def hyperscript-cdn
  "https://unpkg.com/hyperscript.org@0.9.14/dist/_hyperscript.min.js")

(defn page-head []
  [:head
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:link {:rel "stylesheet" :href "/assets/css/chat.css"}]
   [:script {:src tailwind-cdn}]
   [:script {:src "/assets/js/tailwind-config.js"}]
   [:script {:src hyperscript-cdn}]
   [:script {:type "module" :src datastar-cdn}]])

(defn page-shell [body]
  [:html {:data-datastar-root true}
   (page-head)
   body])

(defn home-nav []
  [:div.flex.gap-3.p-2
   [:a {:href "/chat"}
    [:button.bg-accent.text-warm.font-semibold.px-5.py-3.rounded.cursor-pointer.transition-all
     {:class "hover:brightness-110 active:scale-95"}
     "Join Chat"]]
   [:a {:href "/examples"}
    [:button.bg-warm.text-primary.font-semibold.px-5.py-3.rounded.cursor-pointer.transition-all
     {:class "hover:brightness-110 active:scale-95"}
     "Examples"]]])

(def home-page
  (h> (page-shell
       [:body.bg-primary.min-h-screen.p-5
        [:h1.text-light.text-2xl.font-bold.p-4 "Welcome"]
        (home-nav)])))

(defn hello-buttons []
  [:div.flex.flex-wrap.gap-3.p-2
   [:button.bg-accent.text-warm.font-semibold.px-5.py-3.rounded.cursor-pointer.transition-all
    {:class "hover:brightness-110 active:scale-95"
     :data-on:click "@get('/say-hello')"}
    "Press to hello!"]
   [:button.bg-warm.text-primary.font-semibold.px-5.py-3.rounded.cursor-pointer.transition-all
    {:class "hover:brightness-110 active:scale-95"
     :data-on:click "@get('/chunked-hello')"}
    "Press to chunked hello!"]
   [:button.bg-light.text-primary.font-semibold.px-5.py-3.rounded.cursor-pointer.transition-all
    {:class "hover:brightness-110 active:scale-95"
     :data-on:click "@get('/subscribe')"}
    "Press to subscribe hello channel!"]
   [:a {:href "/chat"}
    [:button.bg-warm.text-primary.font-semibold.px-5.py-3.rounded.cursor-pointer.transition-all
     {:class "hover:brightness-110 active:scale-95"}
     "Join Chat"]]])

(def examples-page
  (h> (page-shell
       [:body.bg-primary.min-h-screen.p-5
        (hello-buttons)
        [:p#hello-field.mt-4.text-xl.text-light]])))

(defn username-section []
  [:div#username-section
   [:div.flex.gap-3.items-center
    [:input.flex-1.px-3.py-2.border.rounded.text-light.text-base
     {:class "bg-white/10 border-accent/60 placeholder:text-light/40
              focus:outline-none focus:border-warm focus:ring-2 focus:ring-warm/30"
      :type "text"
      :placeholder "Enter your username..."
      :autofocus true
      :data-bind "username"
      :data-on:keydown "if (evt.key === 'Enter' && $username.trim()) @get('/chat/subscribe')"}]
    [:button.bg-accent.text-warm.font-semibold.px-5.py-2.rounded.cursor-pointer.transition-all
     {:class "hover:brightness-110 active:scale-95"
      :data-on:click "$username.trim() && @get('/chat/subscribe')"}
     "Join"]]])

(defn chat-area []
  [:div#chat-area
   [:div#chat-messages.flex.flex-col.gap-1.overflow-y-auto.p-3.mt-4.border.rounded-lg.scroll-smooth
    {:class "max-h-[60vh] border-accent/40 bg-black/15"
     :_ "on mutation of childList set my scrollTop to my scrollHeight"}]
   [:div.flex.gap-2.items-center.mt-4
    [:input#message-input.flex-1.min-w-0.px-3.py-2.border.rounded.text-light.text-base
     {:class "bg-white/10 border-accent/60 placeholder:text-light/40
              focus:outline-none focus:border-warm focus:ring-2 focus:ring-warm/30"
      :type "text"
      :placeholder "Type a message..."
      :data-bind "message"
      :data-on:keydown "if (evt.key === 'Enter' && $message.trim()) { @post('/chat/send'); $message = '' }"}]
    [:button.bg-accent.text-warm.font-semibold.px-5.py-2.rounded.cursor-pointer.transition-all
     {:class "hover:brightness-110 active:scale-95"
      :data-on:click "if ($message.trim()) { @post('/chat/send'); $message = '' }"}
     "Send"]]])

(def chat-page
  (h> (page-shell
       [:body.bg-primary.min-h-screen.p-5
        [:h1.text-light.text-2xl.font-bold.p-4 "Chat"]
        (username-section)])))
