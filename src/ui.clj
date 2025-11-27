(ns ui
  (:require [clojure.string :as s]
            [hiccup2.core :as h]))

(defn ->kw [styles]
  (->> styles
       (map name)
       (s/join ".")
       keyword))

(def btn-grp-style
  [:div
   :bg-blue-200
   :text-gray-700
   :font-bold
   :py-2
   :px-4
   :rounded
   :cursor-pointer
   :hover:bg-blue-300])

(defn hello-buttons []
  [:div.bg-gray-100.shadow-md.rounded.p-4.mx-auto.w-fit.flex.space-x-4
   [(->kw btn-grp-style)
    {:data-on:click "@get('/say-hello')"}
    "Press to hello!"]
   [(->kw btn-grp-style)
    {:data-on:click "@get('/chunked-hello')"}
    "Press to chuncked hello!"]
   [(->kw btn-grp-style)
    {:data-on:click "@get('/subscribe')"}
    "Press to subscribe hello channel!"]])

(def datastar-cdn
  "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.6/bundles/datastar.js")

(def tailwind-cdn
  "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4")

(def hello-page
  (->> [:html
        {:data-datastar-root true}
        [:head
         [:script
          {:type "module"
           :src  ui/datastar-cdn}]
         [:script
          {:type "module"
           :src  ui/tailwind-cdn}]]
        [:body
         (ui/hello-buttons)
         [:p#hello-field.mt-4.text-xl]]]
       h/html
       str))
